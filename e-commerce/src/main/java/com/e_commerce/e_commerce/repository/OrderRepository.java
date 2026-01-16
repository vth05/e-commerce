package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.dto.response.RevenueByDate;
import com.e_commerce.e_commerce.dto.response.TopCustomersResponse;
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
            select sum(o.totalPrice)
            from Order o
            where o.createdAt between :start and :end and o.checkoutStatus = :status
            """)
    BigDecimal findTotalRevenueByDateRangeAndCheckoutStatus(@Param("status") CheckoutStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
            select new com.e_commerce.e_commerce.dto.response.RevenueByDate(cast(o.createdAt as LocalDate), sum(o.totalPrice))
            from Order o
            where o.createdAt between :start and :end and o.checkoutStatus = :status
            group by function('DATE', o.createdAt)
            order by function('DATE', o.createdAt)
            """)
    List<RevenueByDate> findDailyRevenueByDateRangeAndCheckoutStatus(@Param("status") CheckoutStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
            select u.id, concat(u.first_name, ' ', u.last_name) as full_name, u.email, u.phone_number, sum(o.total_price) as total_spent, count(o.cart_id) as total_orders
            from orders o
            join user u on o.user_id = u.id
            where o.created_at between :start and :end and o.checkout_status = :status and u.active = :active
            group by u.id, concat(u.first_name, ' ', u.last_name), u.email, u.phone_number
            order by total_spent desc
            limit :limit
            """, nativeQuery = true)
        // native query => String status (not CheckoutStatus status)
    List<Object[]> findTopCustomersByUserStatusAndDateRangeAndCheckoutStatus(
            @Param("status") String status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("active") boolean active,
            @Param("limit") int limit
    );
}
