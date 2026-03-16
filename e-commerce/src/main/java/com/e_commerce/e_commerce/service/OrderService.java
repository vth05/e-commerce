package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.response.OrderResponse;
import com.e_commerce.e_commerce.entity.Order;
import com.e_commerce.e_commerce.enums.CheckoutStatus;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.mapper.OrderMapper;
import com.e_commerce.e_commerce.repository.OrderRepository;
import com.e_commerce.e_commerce.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class OrderService {
    OrderRepository orderRepository;
    OrderMapper orderMapper;

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

    public OrderResponse cancelOrder(String id) {
        String userId = SecurityUtils.getUserIdFromAuthentication();
        Order order = orderRepository.findByIdAndUserId(id, userId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
        if (order.getCheckoutStatus() == CheckoutStatus.PAID) {
            throw new AppException(ErrorCode.ORDER_ALREADY_PAID);
        }
        order.setCheckoutStatus(CheckoutStatus.CANCELLED);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }
}
