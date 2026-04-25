package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>, ProductRepositoryCustom {
    Optional<Product> findByIdAndActiveTrue(String productId);

    Page<Product> findAllByActiveTrue(Pageable pageable);

    @Query("""
            select distinct p
            from Product p
            join fetch p.productVariants pv
            where p.active = true and pv.active = true
            """)
    List<Product> findAllWithVariantsForChatbot();
}
