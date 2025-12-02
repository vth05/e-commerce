package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.CheckoutRequest;
import com.e_commerce.e_commerce.dto.response.OrderResponse;
import com.e_commerce.e_commerce.entity.*;
import com.e_commerce.e_commerce.enums.CartStatus;
import com.e_commerce.e_commerce.enums.CheckoutStatus;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.enums.PaymentMethod;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.mapper.OrderMapper;
import com.e_commerce.e_commerce.repository.*;
import com.e_commerce.e_commerce.util.SecurityUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class CheckoutService {
    CartRepository cartRepository;
    VoucherRepository voucherRepository;
    ProductVariantRepository productVariantRepository;
    OrderItemRepository orderItemRepository;
    OrderRepository orderRepository;
    OrderMapper orderMapper;
    CartItemRepository cartItemRepository;
    @NonFinal
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    int batchSize;
    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        // get cart by userId
        String userId = SecurityUtils.getUserIdFromAuthentication();
        Cart cart = cartRepository.findByUserIdAndCartStatus(userId, CartStatus.ACTIVE).orElseThrow(() -> new AppException(ErrorCode.CART_NOT_EXISTED));

        // create new order
        Order order = Order.builder()
                .cart(cart)
                .userId(userId)
                .checkoutStatus(CheckoutStatus.PAID)
                .build();
        // create orderId (= cartId)
        order = orderRepository.save(order);

        // calculate totalPriceOfCart
        BigDecimal totalPriceOfCart = BigDecimal.ZERO;
        List<CartItem> cartItems = cartItemRepository.findAllForCheckout(cart.getId());
        List<Object> entitiesToDetach = new ArrayList<>();
        for (int i = 0; i < cartItems.size(); i++) {
            if (i > 0 && i % batchSize == 0) {
                entityManager.flush();
                for (Object object : entitiesToDetach) {
                    entityManager.detach(object);
                }
                entitiesToDetach.clear();
            }
            CartItem cartItem = cartItems.get(i);
            ProductVariant productVariant = cartItem.getProductVariant();
            long quantityOfCartItem = cartItem.getQuantity();
            long quantityOfProductVariant = productVariant.getQuantity();

            // check stock
            if (quantityOfProductVariant < quantityOfCartItem) {
                throw new AppException(ErrorCode.PRODUCT_VARIANT_INSUFFICIENT_STOCK);
            }

            // reduce stock
            productVariant.setQuantity(quantityOfProductVariant - quantityOfCartItem);
            entitiesToDetach.add(productVariant);

            // get the newest price
            BigDecimal priceAtPurchase = productVariant.getPrice();
            totalPriceOfCart = totalPriceOfCart.add(priceAtPurchase.multiply(BigDecimal.valueOf(quantityOfCartItem)));
            OrderItem orderItem = OrderItem.builder()
                    .productName(productVariant.getProduct().getName())
                    .productId(productVariant.getProduct().getId())
                    .productVariantId(productVariant.getId())
                    .priceAtPurchase(priceAtPurchase)
                    .quantity(quantityOfCartItem)
                    .order(order)
                    .build();
            entityManager.persist(orderItem);
            entitiesToDetach.add(orderItem);

            // for response
            order.getOrderItems().add(orderItem);
        }

        // calculate discount
        BigDecimal discount = BigDecimal.ZERO;
        // optional
        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            Voucher voucher = voucherRepository.findByCodeAndActiveTrue(request.getVoucherCode()).orElseThrow(() -> new AppException(ErrorCode.VOUCHER_CODE_NOT_EXISTED));
            LocalDateTime now = LocalDateTime.now();

            // check usage
            if (voucher.getUsageCount().equals(voucher.getUsageLimit())) {
                throw new AppException(ErrorCode.VOUCHER_OUT_OF);
            }

            // check if the voucher is expired
            if (now.isBefore(voucher.getValidFrom()) || now.isAfter(voucher.getValidTo())) {
                throw new AppException(ErrorCode.VOUCHER_EXPIRED);
            }

            // increase usage
            voucher.setUsageCount(voucher.getUsageCount() + 1);

            // one of the two
            if (voucher.getDiscountAmount() != null) {
                discount = discount.add(BigDecimal.valueOf(voucher.getDiscountAmount()));
            } else if (voucher.getDiscountPercent() != null) {
                discount = discount.add(totalPriceOfCart.multiply(BigDecimal.valueOf(voucher.getDiscountPercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)));
            }

            order.setVoucher(voucher);
        }

        // save cart
        cart.setCartStatus(CartStatus.CHECKED_OUT);

        // save order
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(parsePaymentMethod(request.getPaymentMethod()));
        order.setSubtotal(totalPriceOfCart);
//        order.setShippingFee(ShippingUtils.calculateShippingFee());
        order.setDiscount(discount);
        order.setTotalPrice(totalPriceOfCart
//                .add(order.getShippingFee())
                        .subtract(discount)
                        .setScale(2, RoundingMode.HALF_UP)
        );
        return orderMapper.toOrderResponse(order);
    }

    public Page<OrderResponse> getOrderHistoryOfCurrentUser(CheckoutStatus checkoutStatus, int page, int size, String sortBy, String sortDir) {
        String userId = SecurityUtils.getUserIdFromAuthentication();
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders;
        if (checkoutStatus != null) {
            orders = orderRepository.findAllByUserIdAndCheckoutStatus(userId, checkoutStatus, pageable);
        } else {
            orders = orderRepository.findAllByUserIdAndCheckoutStatusNot(userId, CheckoutStatus.DRAFT, pageable);
        }
        return orders.map(order -> orderMapper.toOrderResponse(order));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderResponse> listOrdersForAdmin(String userId, CheckoutStatus checkoutStatus, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders;
        if (checkoutStatus != null && userId != null) {
            orders = orderRepository.findAllByUserIdAndCheckoutStatus(userId, checkoutStatus, pageable);
        } else if (checkoutStatus != null) {
            orders = orderRepository.findAllByCheckoutStatus(checkoutStatus, pageable);
        } else if (userId != null) {
            orders = orderRepository.findAllByUserId(userId, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return orders.map(order -> orderMapper.toOrderResponse(order));
    }

    public OrderResponse getOrderById(String orderId) {
        String userId = SecurityUtils.getUserIdFromAuthentication();
        Order order = orderRepository.findByIdAndUserId(orderId, userId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
        return orderMapper.toOrderResponse(order);
    }

    private PaymentMethod parsePaymentMethod(String paymentMethod) {
        try {
            return PaymentMethod.valueOf(paymentMethod.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
    }
}
