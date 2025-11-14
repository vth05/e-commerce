package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    Optional<ProductVariant> findByIdAndActiveTrue(String productVariantById);

    Page<ProductVariant> findAllByProductIdAndActiveTrue(String productId, Pageable pageable);

    Page<ProductVariant> findAllByProductId(String productId, Pageable pageable);
}
