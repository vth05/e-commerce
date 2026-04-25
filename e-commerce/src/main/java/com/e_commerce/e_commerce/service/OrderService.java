package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.response.OrderResponse;
import com.e_commerce.e_commerce.entity.Order;
import com.e_commerce.e_commerce.entity.OrderItem;
import com.e_commerce.e_commerce.entity.ProductVariant;
import com.e_commerce.e_commerce.enums.CheckoutStatus;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.mapper.OrderMapper;
import com.e_commerce.e_commerce.repository.OrderItemRepository;
import com.e_commerce.e_commerce.repository.OrderRepository;
import com.e_commerce.e_commerce.repository.ProductVariantRepository;
import com.e_commerce.e_commerce.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class OrderService {
    OrderRepository orderRepository;
    OrderMapper orderMapper;
    OrderItemRepository orderItemRepository;
    ProductVariantRepository productVariantRepository;

    public Page<OrderResponse> getOrderHistoryOfCurrentUser(CheckoutStatus checkoutStatus, int page, int size, String sortBy, String sortDir) {
        String userId = SecurityUtils.getUserIdFromAuthentication();
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders;
        if (checkoutStatus != null) {
            orders = orderRepository.findAllByUserIdAndCheckoutStatus(userId, checkoutStatus, pageable);
        } else {
            orders = orderRepository.findAllByUserId(userId, pageable);
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

    @Transactional
    public OrderResponse cancelOrder(String id) {
        String userId = SecurityUtils.getUserIdFromAuthentication();
        Order order = orderRepository.findByIdAndUserId(id, userId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
        if (order.getCheckoutStatus() != CheckoutStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
        }
        restoreProductVariantStock(order);
        order.setCheckoutStatus(CheckoutStatus.CANCELLED);
        return orderMapper.toOrderResponse(order);
    }

    private void restoreProductVariantStock(Order order) {
        List<OrderItem> orderItemList = orderItemRepository.findAllByOrderId(order.getId());
        Map<String, Long> productVariantIdToQuantityMap = orderItemList.stream()
                .collect(Collectors.toMap(
                        OrderItem::getProductVariantId,
                        OrderItem::getQuantity
                ));
        List<ProductVariant> productVariants = productVariantRepository.findAllByIdIn(productVariantIdToQuantityMap.keySet());
        for (ProductVariant productVariant : productVariants) {
            productVariant.setQuantity(productVariant.getQuantity() + productVariantIdToQuantityMap.get(productVariant.getId()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public OrderResponse updateOrderStatus(String orderId, CheckoutStatus newStatus) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
        validateStatusTransition(order.getCheckoutStatus(), newStatus);
        if (newStatus == CheckoutStatus.DELIVERY_FAILED) {
            restoreProductVariantStock(order);
        }
        order.setCheckoutStatus(newStatus);
        return orderMapper.toOrderResponse(order);
    }

    private void validateStatusTransition(CheckoutStatus current, CheckoutStatus next) {
        Map<CheckoutStatus, Set<CheckoutStatus>> allowedTransitions = Map.of(
                CheckoutStatus.PENDING, Set.of(CheckoutStatus.PAID, CheckoutStatus.CANCELLED),
                CheckoutStatus.PAID, Set.of(CheckoutStatus.SHIPPING),
                CheckoutStatus.SHIPPING, Set.of(CheckoutStatus.DELIVERED, CheckoutStatus.DELIVERY_FAILED),
                CheckoutStatus.DELIVERED, Set.of(),
                CheckoutStatus.DELIVERY_FAILED, Set.of(),
                CheckoutStatus.CANCELLED, Set.of()
        );
        Set<CheckoutStatus> allowed = allowedTransitions.getOrDefault(current, Set.of());
        if (!allowed.contains(next)) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
        }
    }
}
