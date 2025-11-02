package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
//    Optional<CartItem> findByCartAndProductAndPriceAtPurchaseAndActive(Cart cart, Product product, BigDecimal priceAtPurchase, boolean active);
}
