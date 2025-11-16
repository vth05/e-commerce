package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    Optional<CartItem> findByCartIdAndProductVariantIdAndActiveTrue(String cartId, String productVariantId);

    Optional<CartItem> findByIdAndActiveTrue(String cartItemId);

    List<CartItem> findAllByCartIdAndActiveTrue(String cartId);
}
