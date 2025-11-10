package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.entity.Order;
import com.e_commerce.e_commerce.enums.CheckoutStatus;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findAllByUserIdAndCheckoutStatusNot(String userId, CheckoutStatus checkoutStatus);

    List<Order> findAllByUserIdAndCheckoutStatus(String userId, CheckoutStatus checkoutStatus);

    List<Order> findAllByCheckoutStatus(CheckoutStatus checkoutStatus);

    List<Order> findAllByUserId(String userId);

    Optional<Order> findByIdAndUserId(String orderId, String userId);
}
