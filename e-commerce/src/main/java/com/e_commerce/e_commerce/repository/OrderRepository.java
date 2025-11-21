package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.entity.Order;
import com.e_commerce.e_commerce.enums.CheckoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Page<Order> findAllByUserIdAndCheckoutStatusNot(String userId, CheckoutStatus checkoutStatus, Pageable pageable);

    Page<Order> findAllByUserIdAndCheckoutStatus(String userId, CheckoutStatus checkoutStatus, Pageable pageable);

    Page<Order> findAllByCheckoutStatus(CheckoutStatus checkoutStatus, Pageable pageable);

    Page<Order> findAllByUserId(String userId, Pageable pageable);

    Optional<Order> findByIdAndUserId(String orderId, String userId);
}
