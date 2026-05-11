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
    // discountAmount is null when no voucher is applied; without COALESCE, any arithmetic with null returns null, causing incorrect revenue calculation
    @Query("""
            select new com.e_commerce.e_commerce.dto.response.ProductRevenueResponse(oi.productName, sum(oi.priceAtPurchase * oi.quantity - COALESCE(oi.discountAmount, 0)))
            from OrderItem oi
            join oi.order o
            where o.createdAt between :start and :end and o.checkoutStatus = :status
            group by oi.productName
            """)
    Page<ProductRevenueResponse> findRevenueByProductAndDateRangeAndCheckoutStatus(@Param("status") CheckoutStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query("""
            select new com.e_commerce.e_commerce.dto.response.TopProductsByRevenueResponse(oi.productName, sum(oi.priceAtPurchase * oi.quantity - COALESCE(oi.discountAmount, 0)))
            from OrderItem oi
            join oi.order o
            where o.createdAt between :start and :end and o.checkoutStatus = :status
            group by oi.productName
            order by sum(oi.priceAtPurchase * oi.quantity - COALESCE(oi.discountAmount, 0)) desc
            """)
    Page<TopProductsByRevenueResponse> findTopProductsByRevenueAndDateRangeAndCheckoutStatus(@Param("status") CheckoutStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query("""
            select new com.e_commerce.e_commerce.dto.response.TopProductsByQuantitySoldResponse(oi.productName, sum(oi.quantity))
            from OrderItem oi
            join oi.order o
            where o.createdAt between :start and :end and o.checkoutStatus = :status
            group by oi.productName
            order by sum(oi.quantity) desc
            """)
    Page<TopProductsByQuantitySoldResponse> findTopProductsByQuantitySoldAndDateRangeAndCheckoutStatus(@Param("status") CheckoutStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    boolean existsByProductIdAndOrderUserIdAndOrderCheckoutStatus(String productId, String userId, CheckoutStatus checkoutStatus);

    @Query(value = """
            select oi.product_id,
                   oi.product_name,
                   pv.display_price,
                   pv.stock_quantity,
                   sum(oi.quantity) as quantitySold
            from order_item oi
            join (
                select product_id,
                       min(price) as display_price,
                       sum(quantity) as stock_quantity
                from product_variant
                group by product_id
                having stock_quantity > 0
            ) pv on oi.product_id = pv.product_id
            join orders o on oi.order_id = o.cart_id
            where o.created_at between :start and :end and o.checkout_status in ('PAID', 'SHIPPING', 'DELIVERED')
            group by oi.product_id, oi.product_name, pv.display_price, pv.stock_quantity
            order by quantitySold desc
            limit :limit
            """, nativeQuery = true)
    List<Object[]> findTopSellingProductsForRecommendationByQuantitySoldAndDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("limit") int limit
    );

    List<OrderItem> findAllByOrderId(String orderId);
}
