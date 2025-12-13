package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.dto.response.ProductRevenueResponse;
import com.e_commerce.e_commerce.dto.response.TopProductsByQuantitySoldResponse;
import com.e_commerce.e_commerce.dto.response.TopProductsByRevenueResponse;
import com.e_commerce.e_commerce.entity.OrderItem;
import com.e_commerce.e_commerce.enums.CheckoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    @Query("""
            select new com.e_commerce.e_commerce.dto.response.ProductRevenueResponse(oi.productName, sum(oi.priceAtPurchase * oi.quantity))
            from OrderItem oi
            join oi.order o
            where o.createdAt >= :start and o.createdAt <= :end and o.checkoutStatus = :status
            group by oi.productName
            """)
    List<ProductRevenueResponse> findRevenueByProductAndDateRangeAndCheckoutStatus(@Param("status") CheckoutStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
            select new com.e_commerce.e_commerce.dto.response.TopProductsByRevenueResponse(oi.productName, sum(oi.priceAtPurchase * oi.quantity))
            from OrderItem oi
            join oi.order o
            where o.createdAt >= :start and o.createdAt <= :end and o.checkoutStatus = :status
            group by oi.productName
            order by sum(oi.priceAtPurchase * oi.quantity) desc
            """)
    Page<TopProductsByRevenueResponse> findTopProductsByRevenueAndDateRangeAndCheckoutStatus(@Param("status") CheckoutStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query("""
            select new com.e_commerce.e_commerce.dto.response.TopProductsByQuantitySoldResponse(oi.productName, sum(oi.quantity))
            from OrderItem oi
            join oi.order o
            where o.createdAt >= :start and o.createdAt <= :end and o.checkoutStatus = :status
            group by oi.productName
            order by sum(oi.quantity) desc
            """)
    Page<TopProductsByQuantitySoldResponse> findTopProductsByQuantitySoldAndDateRangeAndCheckoutStatus(@Param("status") CheckoutStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query(value = """
            select oi.product_id,
                   oi.product_name,
                   pv.displayPrice,
                   pv.stockQuantity,
                   sum(oi.quantity) as quantitySold
            from order_item oi
            join (
                select product_id,
                       min(price) as displayPrice,
                       sum(quantity) as stockQuantity
                from product_variant
                group by product_id
            ) pv on oi.product_id = pv.product_id
            join orders o on oi.order_id = o.cart_id
            where oi.created_at >= :start and oi.created_at <= :end and o.checkout_status in ('PAID', 'PENDING')
            group by oi.product_id, oi.product_name, pv.displayPrice, pv.stockQuantity
            order by quantitySold desc
            limit :limit
            """, nativeQuery = true)
    List<Object[]> findTopSellingProductsForRecommendationByQuantitySoldAndDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("limit") int limit
    );
}
