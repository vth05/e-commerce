package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.entity.Cart;
import com.e_commerce.e_commerce.entity.CartItem;
import com.e_commerce.e_commerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
