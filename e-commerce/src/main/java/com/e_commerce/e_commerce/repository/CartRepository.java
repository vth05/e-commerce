package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, String> {
//    Optional<Cart> findByUserIdAndCartStatus(String userId, CartStatus cartStatus);
}
