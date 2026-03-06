package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.*;
import com.e_commerce.e_commerce.dto.response.CheckoutPreviewResponse;
import com.e_commerce.e_commerce.dto.response.OrderResponse;
import com.e_commerce.e_commerce.dto.response.ShippingFeeCalculationResult;
import com.e_commerce.e_commerce.entity.*;
import com.e_commerce.e_commerce.enums.CartStatus;
import com.e_commerce.e_commerce.enums.CheckoutStatus;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.mapper.OrderMapper;
import com.e_commerce.e_commerce.repository.*;
import com.e_commerce.e_commerce.util.CheckoutUtils;
import com.e_commerce.e_commerce.util.ParseUtils;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class CheckoutService {
    CartRepository cartRepository;
    VoucherRepository voucherRepository;
    OrderRepository orderRepository;
    OrderMapper orderMapper;
    CartItemRepository cartItemRepository;
    CheckoutUtils checkoutUtils;
    @NonFinal
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    int batchSize;
    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        String userId = SecurityUtils.getUserIdFromAuthentication();
        Cart cart = cartRepository.findByUserIdAndCartStatus(userId, CartStatus.ACTIVE).orElseThrow(() -> new AppException(ErrorCode.CART_NOT_EXISTED));
        Order order = Order.builder()
                .cart(cart)
                .userId(userId)
                .checkoutStatus(CheckoutStatus.PENDING)
                .build();
        // create order's id (= cart's id)
        order = orderRepository.save(order);
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal subtotal = BigDecimal.ZERO;
        List<String> cartItemIdsFromRequest = request.getCartItemIds();
        List<CartItem> cartItemsFromRequest = cartItemRepository.findAllCartItemsFromRequest(cart.getId(), cartItemIdsFromRequest);
        if (cartItemIdsFromRequest.size() != cartItemsFromRequest.size()) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_IN_CART);
        }
        Map<String, String> cartItemToVoucherCodeMap = Optional.ofNullable(request.getCartItemIdToVoucherCodeMap()).orElse(Collections.emptyMap());
        List<Voucher> selectedVouchers = voucherRepository.findAllByCodeIn(cartItemToVoucherCodeMap.values());
        Map<String, Voucher> voucherLookup = selectedVouchers.stream().collect(Collectors.toMap(voucher -> voucher.getCode(), voucher -> voucher));
        List<Object> entitiesToDetach = new ArrayList<>();
        for (int i = 0; i < cartItemsFromRequest.size(); i++) {
            if (i > 0 && i % batchSize == 0) {
                entityManager.flush();
                for (Object object : entitiesToDetach) {
                    entityManager.detach(object);
                }
                entitiesToDetach.clear();
            }
            CartItem cartItem = cartItemsFromRequest.get(i);
            long cartItemQuantity = cartItem.getQuantity();
            ProductVariant productVariant = cartItem.getProductVariant();
            long productVariantQuantity = productVariant.getQuantity();
            if (productVariantQuantity < cartItemQuantity) {
                throw new AppException(ErrorCode.PRODUCT_VARIANT_INSUFFICIENT_STOCK);
            }
            productVariant.setQuantity(productVariantQuantity - cartItemQuantity);
            entitiesToDetach.add(productVariant);
            BigDecimal currentPrice = productVariant.getPrice();
            Product product = productVariant.getProduct();
            OrderItem orderItem = OrderItem.builder()
                    .productName(product.getName())
                    .productId(product.getId())
                    .productVariantId(productVariant.getId())
                    .priceAtPurchase(currentPrice)
                    .quantity(cartItemQuantity)
                    .order(order)
                    .build();
            BigDecimal cartItemSubtotal = currentPrice.multiply(BigDecimal.valueOf(cartItemQuantity));
            String voucherCode = cartItemToVoucherCodeMap.get(cartItem.getId());
            if (voucherCode != null) {
                Voucher voucher = voucherLookup.get(voucherCode);
                // if this voucherCode doesn't exist in database
                if (voucher == null) {
                    throw new AppException(ErrorCode.VOUCHER_CODE_NOT_EXISTED);
                } else {
                    if (checkoutUtils.isVoucherApplicableToProduct(voucher, product)) {
                        BigDecimal discount = checkoutUtils.calculateCartItemDiscount(voucher, cartItemSubtotal);
                        totalDiscount = totalDiscount.add(discount);
                        voucher.setUsageCount(voucher.getUsageCount() + 1);
                        orderItem.setVoucherCode(voucherCode);
                        orderItem.setDiscountAmount(discount);
                    }
                }
            }
            subtotal = subtotal.add(cartItemSubtotal);
            entityManager.persist(orderItem);
            entitiesToDetach.add(orderItem);
            // for response
            order.getOrderItems().add(orderItem);
        }
        entityManager.flush();
        ShippingFeeCalculationResult shippingFeeCalculationResult = checkoutUtils.calculateShippingFeeAndDimensions(subtotal, request.getService_id(), request.getTo_ward_code(), request.getTo_district_id(), cartItemsFromRequest);
        BigDecimal originalShippingFee = shippingFeeCalculationResult.originalShippingFee();
        String shippingVoucherCode = request.getShippingVoucherCode();
        BigDecimal shippingDiscount = checkoutUtils.calculateShippingDiscount(subtotal, originalShippingFee, shippingVoucherCode);
        BigDecimal shippingFee = originalShippingFee.subtract(shippingDiscount);
        cart.setCartStatus(CartStatus.CHECKED_OUT);
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(ParseUtils.parsePaymentMethod(request.getPaymentMethod()));
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setDiscount(totalDiscount);
        order.setTotalPrice(subtotal.subtract(totalDiscount).add(shippingFee).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
        return orderMapper.toOrderResponse(order);
    }

    public CheckoutPreviewResponse checkoutPreview(CheckoutPreviewRequest request) {
        String userId = SecurityUtils.getUserIdFromAuthentication();
        Cart currentCart = cartRepository.findByUserIdAndCartStatus(userId, CartStatus.ACTIVE).orElseThrow(() -> new AppException(ErrorCode.CART_NOT_EXISTED));
        String currentCartId = currentCart.getId();
        List<String> cartItemIdsFromRequest = request.getCartItemIds();
        List<CartItem> cartItemsFromRequest = cartItemRepository.findAllCartItemsFromRequest(currentCartId, cartItemIdsFromRequest);
        if (cartItemIdsFromRequest.size() != cartItemsFromRequest.size()) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_IN_CART);
        }
        Map<String, String> cartItemToVoucherCodeMap = Optional.ofNullable(request.getCartItemIdToVoucherCodeMap()).orElse(Collections.emptyMap());
        List<Voucher> selectedVouchers = voucherRepository.findAllByCodeIn(cartItemToVoucherCodeMap.values());
        Map<String, Voucher> voucherLookup = selectedVouchers.stream().collect(Collectors.toMap(voucher -> voucher.getCode(), voucher -> voucher));
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cartItemsFromRequest) {
            ProductVariant productVariant = cartItem.getProductVariant();
            BigDecimal currentPrice = productVariant.getPrice();
            String voucherCode = cartItemToVoucherCodeMap.get(cartItem.getId());
            BigDecimal cartItemSubtotal = currentPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            if (voucherCode != null) {
                Voucher voucher = voucherLookup.get(voucherCode);
                // if this voucherCode doesn't exist in database
                if (voucher == null) {
                    throw new AppException(ErrorCode.VOUCHER_CODE_NOT_EXISTED);
                } else {
                    if (checkoutUtils.isVoucherApplicableToProduct(voucher, productVariant.getProduct())) {
                        BigDecimal discount = checkoutUtils.calculateCartItemDiscount(voucher, cartItemSubtotal);
                        totalDiscount = totalDiscount.add(discount);
                    }
                }
            }
            subtotal = subtotal.add(cartItemSubtotal);
        }
        ShippingFeeCalculationResult shippingFeeCalculationResult = checkoutUtils.calculateShippingFeeAndDimensions(subtotal, request.getService_id(), request.getTo_ward_code(), request.getTo_district_id(), cartItemsFromRequest);
        BigDecimal originalShippingFee = shippingFeeCalculationResult.originalShippingFee();
        String shippingVoucherCode = request.getShippingVoucherCode();
        BigDecimal shippingDiscount = checkoutUtils.calculateShippingDiscount(subtotal, originalShippingFee, shippingVoucherCode);
        return CheckoutPreviewResponse.builder()
                .subtotal(subtotal)
                .discount(totalDiscount)
                .shippingFee(originalShippingFee)
                .shippingDiscount(shippingDiscount)
                .totalPrice(subtotal.subtract(totalDiscount).add(originalShippingFee.subtract(shippingDiscount)).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP))
                .build();
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
}
