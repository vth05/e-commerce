package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.dto.response.ProductVariantLowStockResponse;
import com.e_commerce.e_commerce.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    Optional<ProductVariant> findByIdAndActiveTrue(String productVariantById);

    Page<ProductVariant> findAllByProductIdAndActiveTrue(String productId, Pageable pageable);

    Page<ProductVariant> findAllByProductId(String productId, Pageable pageable);

    long countByIdIn(Set<String> ids);

    List<ProductVariant> findAllByIdIn(Set<String> productVariantIds);

    @Query("""
            select new com.e_commerce.e_commerce.dto.response.ProductVariantLowStockResponse(p.id, p.name, pv.id, pv.quantity)
            from ProductVariant pv
            join pv.product p
            where pv.quantity <= :threshold
            """)
    Page<ProductVariantLowStockResponse> findProductVariantsLowInStock(@Param("threshold") int threshold, Pageable pageable);
}
