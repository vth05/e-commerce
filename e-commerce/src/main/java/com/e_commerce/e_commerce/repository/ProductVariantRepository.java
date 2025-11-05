package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    Optional<ProductVariant> findByIdAndActiveTrue(String productVariantById);

    List<ProductVariant> findAllByProductIdAndActiveTrue(String productId);

    List<ProductVariant> findAllByProductId(String productId);
}
