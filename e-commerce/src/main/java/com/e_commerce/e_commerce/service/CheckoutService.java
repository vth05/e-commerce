package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.*;
import com.e_commerce.e_commerce.dto.response.CheckoutInternalResponse;
import com.e_commerce.e_commerce.dto.response.CheckoutPreviewResponse;
import com.e_commerce.e_commerce.dto.response.OrderResponse;
import com.e_commerce.e_commerce.dto.response.ShippingFeeCalculationResult;
import com.e_commerce.e_commerce.entity.*;
import com.e_commerce.e_commerce.enums.CartStatus;
import com.e_commerce.e_commerce.enums.CheckoutStatus;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.enums.PaymentMethod;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.mapper.OrderMapper;
import com.e_commerce.e_commerce.repository.*;
import com.e_commerce.e_commerce.util.CheckoutUtils;
import com.e_commerce.e_commerce.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
    GhnService ghnService;
    CheckoutUtils checkoutUtils;
    CheckoutTransactionalService checkoutTransactionalService;

    public OrderResponse checkout(CheckoutRequest request) {
        CheckoutInternalResponse response = checkoutTransactionalService.checkoutInternal(request);
        Order order = response.order();
        if (checkoutUtils.parsePaymentMethod(request.getPaymentMethod()) == PaymentMethod.COD) {
            try {
                ShippingFeeCalculationResult shippingFeeCalculationResult = response.shippingFeeCalculationResult();
                GhnCreateOrderRequest ghnCreateOrderRequest = GhnCreateOrderRequest.builder()
                        .from_name("MyShop")
                        .to_name(request.getReceiverName())
                        .from_phone("0902163987")
                        .to_phone(request.getReceiverPhone())
                        .from_address("53E đường số 8, Phường Bình Trị Đông, Quận Bình Tân, Hồ Chí Minh")
                        .to_address(request.getShippingAddress())
                        .from_ward_name("Phường Bình Trị Đông")
                        .from_district_name("Quận Bình Tân")
                        .from_province_name("Hồ Chí Minh")
                        .to_ward_code(request.getTo_ward_code())
                        .to_district_id(request.getTo_district_id())
                        .weight(shippingFeeCalculationResult.weightInGrams())
                        .length(shippingFeeCalculationResult.lengthInCm())
                        .width(shippingFeeCalculationResult.widthInCm())
                        .height(shippingFeeCalculationResult.heightInCm())
                        .service_type_id(2)
                        .payment_type_id(2)
                        .required_note(request.getRequired_note())
                        .items(response.items())
                        .build();
                ghnService.createOrder(ghnCreateOrderRequest);
            } catch (Exception e) {
                order.setCheckoutStatus(CheckoutStatus.CREATE_SHIPPING_ORDER_FAILED);
                orderRepository.save(order);
            }
        }
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
