package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.CheckoutRequest;
import com.e_commerce.e_commerce.dto.request.GhnOrderItem;
import com.e_commerce.e_commerce.dto.response.CheckoutInternalResponse;
import com.e_commerce.e_commerce.dto.response.ShippingFeeCalculationResult;
import com.e_commerce.e_commerce.entity.*;
import com.e_commerce.e_commerce.enums.CartStatus;
import com.e_commerce.e_commerce.enums.CheckoutStatus;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.repository.CartItemRepository;
import com.e_commerce.e_commerce.repository.CartRepository;
import com.e_commerce.e_commerce.repository.OrderRepository;
import com.e_commerce.e_commerce.repository.VoucherRepository;
import com.e_commerce.e_commerce.util.CheckoutUtils;
import com.e_commerce.e_commerce.util.SecurityUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CheckoutTransactionalService {
    CartRepository cartRepository;
    OrderRepository orderRepository;
    CartItemRepository cartItemRepository;
    VoucherRepository voucherRepository;
    CheckoutUtils checkoutUtils;
    @NonFinal
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    int batchSize;
    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    public CheckoutInternalResponse checkoutInternal(CheckoutRequest request) {
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
        List<GhnOrderItem> items = new ArrayList<>();
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
            items.add(GhnOrderItem.builder()
                    .name(productVariant.getProductVariantName())
                    .quantity((int) cartItemQuantity)
                    .weight(checkoutUtils.safe(productVariant.getWeight()).setScale(0, RoundingMode.CEILING).intValueExact())
                    .build());
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
        order.setPaymentMethod(checkoutUtils.parsePaymentMethod(request.getPaymentMethod()));
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setDiscount(totalDiscount);
        order.setTotalPrice(subtotal.subtract(totalDiscount).add(shippingFee).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
        return CheckoutInternalResponse.builder()
                .order(order)
                .shippingFeeCalculationResult(shippingFeeCalculationResult)
                .items(items)
                .build();
    }
}
