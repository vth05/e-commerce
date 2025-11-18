package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.AddProductVariantToCartRequest;
import com.e_commerce.e_commerce.dto.response.CartItemResponse;
import com.e_commerce.e_commerce.dto.response.CartResponse;
import com.e_commerce.e_commerce.entity.Cart;
import com.e_commerce.e_commerce.entity.CartItem;
import com.e_commerce.e_commerce.entity.ProductVariant;
import com.e_commerce.e_commerce.enums.CartStatus;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.repository.CartItemRepository;
import com.e_commerce.e_commerce.repository.CartRepository;
import com.e_commerce.e_commerce.repository.ProductVariantRepository;
import com.e_commerce.e_commerce.util.CartUtils;
import com.e_commerce.e_commerce.util.SecurityUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class CartService {
    CartRepository cartRepository;
    ProductVariantRepository productVariantRepository;
    CartItemRepository cartItemRepository;
    @NonFinal
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    int batchSize;
    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    public CartResponse addCartItemToCart(AddProductVariantToCartRequest request) {
        String productVariantId = request.getProductVariantId();
        ProductVariant productVariant = productVariantRepository.findByIdAndActiveTrue(productVariantId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED));
        long quantityFromRequest = request.getQuantity();
        if (productVariant.getQuantity() < quantityFromRequest) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_INSUFFICIENT_STOCK);
        }

        String userId = SecurityUtils.getUserIdFromAuthentication();
        Cart cart = cartRepository.findByUserIdAndCartStatus(userId, CartStatus.ACTIVE).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .userId(userId)
                    .cartStatus(CartStatus.ACTIVE)
                    .build();
            // create cartId to find cartItem later
            return cartRepository.save(newCart);
        });

        CartItem cartItem = cartItemRepository.findByCartIdAndProductVariantIdAndActiveTrue(cart.getId(), productVariantId).orElseGet(() -> {
            CartItem newCartItem = CartItem.builder()
                    .cart(cart)
                    .productVariant(productVariant)
                    .quantity(0)
                    .build();
            return newCartItem;
        });
        if (productVariant.getQuantity() < quantityFromRequest + cartItem.getQuantity()) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_INSUFFICIENT_STOCK);
        }

        // update quantity of cartItem
        cartItem.setQuantity(cartItem.getQuantity() + quantityFromRequest);
        cartItem.setPriceAtPurchase(productVariant.getPrice());
        cartItemRepository.save(cartItem);

        // update updatedAt field (@LastModifiedDate doesn't work, I don't know, so I do it manually)
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        List<CartItem> cartItems = cartItemRepository.findAllByCartIdAndActiveTrue(cart.getId());
        return buildCartResponse(cart, cartItems);
    }

    @Transactional
    public CartResponse deleteCartItemFromCart(String cartItemId) {
        CartItem cartItem = cartItemRepository.findByIdAndActiveTrue(cartItemId).orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_EXISTED));

        String userId = SecurityUtils.getUserIdFromAuthentication();
        Cart cart = cartRepository.findByUserIdAndCartStatus(userId, CartStatus.ACTIVE).orElseThrow(() -> new AppException(ErrorCode.CART_NOT_EXISTED));
        // ensure the cart item belongs to the same cart as the current user’s request
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // soft delete cartItem
        cartItem.setActive(false);
        cartItemRepository.save(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        List<CartItem> cartItems = cartItemRepository.findAllByCartIdAndActiveTrue(cart.getId());
        return buildCartResponse(cart, cartItems);
    }

    @Transactional
    public void deleteCart() {
        String userId = SecurityUtils.getUserIdFromAuthentication();
        Cart cart = cartRepository.findByUserIdAndCartStatus(userId, CartStatus.ACTIVE).orElseThrow(() -> new AppException(ErrorCode.CART_NOT_EXISTED));

        List<CartItem> cartItems = cartItemRepository.findAllByCartIdAndActiveTrue(cart.getId());
//        for (CartItem cartItem : cartItems) {
//            cartItem.setActive(false);
//            cartItemRepository.save(cartItem);
//        }

        for (int i = 0; i < cartItems.size(); i++) {
            CartItem cartItem = cartItems.get(i);
            if (i > 0 && i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
            cartItem.setActive(false);
            entityManager.merge(cartItem);
        }
        entityManager.flush();
        entityManager.clear();

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    public CartResponse getCurrentCart() {
        String userId = SecurityUtils.getUserIdFromAuthentication();
        Cart cart = cartRepository.findByUserIdAndCartStatus(userId, CartStatus.ACTIVE).orElseThrow(() -> new AppException(ErrorCode.CART_NOT_EXISTED));

        List<CartItem> cartItems = cartItemRepository.findAllByCartIdAndActiveTrue(cart.getId());
        return buildCartResponse(cart, cartItems);
    }

    private List<CartItemResponse> cartItemListToCartItemResponseList(List<CartItem> cartItems) {
        List<CartItemResponse> responses = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            ProductVariant productVariant = cartItem.getProductVariant();
            CartItemResponse response = CartItemResponse.builder()
                    .id(cartItem.getId())
                    .productVariantId(productVariant.getId())
                    .productName(productVariant.getProduct().getName())
                    .productVariantName(productVariant.getProductVariantName())
                    .priceAtPurchase(cartItem.getPriceAtPurchase())
                    .quantity(cartItem.getQuantity())
                    .totalPrice(cartItem.getPriceAtPurchase().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .active(cartItem.isActive())
                    .build();
            responses.add(response);
        }
        return responses;
    }

    private CartResponse buildCartResponse(Cart cart, List<CartItem> cartItems) {
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .cartStatus(String.valueOf(cart.getCartStatus()))
                .totalPrice(CartUtils.calculateTotalPrice(cartItems))
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .cartItems(cartItemListToCartItemResponseList(cartItems))
                .build();
    }
}
