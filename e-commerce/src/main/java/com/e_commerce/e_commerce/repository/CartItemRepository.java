package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    Optional<CartItem> findByCartIdAndProductVariantIdAndActiveTrue(String cartId, String productVariantId);

    Optional<CartItem> findByIdAndActiveTrue(String cartItemId);

    List<CartItem> findAllByCartIdAndActiveTrue(String cartId);

    @Query("""
            select ci
            from CartItem ci
            join fetch ci.productVariant pv
            where ci.cart.id = :cartId and ci.active = true and ci.id in :cartItemIds
            """)
    List<CartItem> findAllCartItemsFromRequest(@Param("cartId") String cartId, @Param("cartItemIds") Collection<String> cartItemIds);
}
