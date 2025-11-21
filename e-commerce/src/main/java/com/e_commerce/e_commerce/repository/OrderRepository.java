package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.dto.response.RevenueByDate;
import com.e_commerce.e_commerce.entity.Order;
import com.e_commerce.e_commerce.enums.CheckoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Page<Order> findAllByUserIdAndCheckoutStatusNot(String userId, CheckoutStatus checkoutStatus, Pageable pageable);

    Page<Order> findAllByUserIdAndCheckoutStatus(String userId, CheckoutStatus checkoutStatus, Pageable pageable);

    Page<Order> findAllByCheckoutStatus(CheckoutStatus checkoutStatus, Pageable pageable);

    Page<Order> findAllByUserId(String userId, Pageable pageable);

    Optional<Order> findByIdAndUserId(String orderId, String userId);

    @Query("""
            select sum(o.totalPrice) from Order o
            where o.checkoutStatus = :status and o.createdAt >= :start and o.createdAt <= :end
            """)
    BigDecimal getTotalRevenue(@Param("status") CheckoutStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
            select new com.e_commerce.e_commerce.dto.response.RevenueByDate(cast(o.createdAt as LocalDate), sum(o.totalPrice))
            from Order o
            where o.checkoutStatus = :status and o.createdAt >= :start and o.createdAt <= :end
            group by function('DATE', o.createdAt)
            order by function('DATE', o.createdAt)
            """)
    List<RevenueByDate> getRevenueByDate(@Param("status") CheckoutStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
