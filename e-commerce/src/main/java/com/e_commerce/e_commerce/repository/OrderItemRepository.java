package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.dto.response.ProductRevenueResponse;
import com.e_commerce.e_commerce.dto.response.TopProductResponse;
import com.e_commerce.e_commerce.entity.OrderItem;
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
            where oi.createdAt >= :start and oi.createdAt <= :end
            group by oi.productName
            """)
    List<ProductRevenueResponse> findRevenueByProductAndDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
            select new com.e_commerce.e_commerce.dto.response.TopProductResponse(oi.productName, sum(oi.priceAtPurchase * oi.quantity))
            from OrderItem oi
            where oi.createdAt >= :start and oi.createdAt <= :end
            group by oi.productName
            order by sum(oi.priceAtPurchase * oi.quantity) desc
            """)
    Page<TopProductResponse> findTopProductsByRevenueAndDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
}
