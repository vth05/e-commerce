package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.AddItemToCartRequest;
import com.e_commerce.e_commerce.dto.response.CartItemResponse;
import com.e_commerce.e_commerce.dto.response.CartResponse;
import com.e_commerce.e_commerce.entity.Cart;
import com.e_commerce.e_commerce.entity.CartItem;
import com.e_commerce.e_commerce.entity.Product;
import com.e_commerce.e_commerce.enums.CartStatus;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.mapper.CartMapper;
import com.e_commerce.e_commerce.repository.CartItemRepository;
import com.e_commerce.e_commerce.repository.CartRepository;
import com.e_commerce.e_commerce.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class CartService {
    CartRepository cartRepository;
    ProductRepository productRepository;
    CartItemRepository cartItemRepository;
    CartMapper cartMapper;

    @Transactional
    public CartResponse addItemToCart(AddItemToCartRequest addItemToCartRequest) {
        Product product = productRepository.findById(addItemToCartRequest.getProductId()).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        long quantityFromRequest = addItemToCartRequest.getQuantity();
        if (product.getQuantity() < quantityFromRequest) {
            throw new AppException(ErrorCode.PRODUCT_INSUFFICIENT_STOCK);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getClaimAsString("userId");
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .userId(userId)
                    .cartStatus(CartStatus.ACTIVE)
                    // avoid NullPointerException when calculating total price
                    .totalPrice(BigDecimal.ZERO)
                    .cartItems(new HashSet<>())
                    .build();
            // create cart's id to find cartItem later
            return cartRepository.save(newCart);
        });

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product).orElseGet(() -> {
            return CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .priceAtPurchase(product.getPrice())
                    .quantity(0)
                    .build();
        });

        cartItem.setQuantity(cartItem.getQuantity() + quantityFromRequest);
        cartItemRepository.save(cartItem);

        // update totalPrice of cart
        BigDecimal totalPriceFromRequest = product.getPrice().multiply(BigDecimal.valueOf(quantityFromRequest));
        cart.setTotalPrice(cart.getTotalPrice().add(totalPriceFromRequest));
        // for mapper
        cart.getCartItems().add(cartItem);
        cartRepository.save(cart);

        // update quantity of product
        product.setQuantity(product.getQuantity() - quantityFromRequest);
        productRepository.save(product);

        Set<CartItem> cartItems = cart.getCartItems();
        return CartResponse.builder()
                .id(cart.getId())
                .userId(userId)
                .cartStatus(String.valueOf(cart.getCartStatus()))
                .totalPrice(cart.getTotalPrice())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .cartItems(cartItemSetToCartItemResponseSet(cartItems))
                .build();
    }

    private Set<CartItemResponse> cartItemSetToCartItemResponseSet(Set<CartItem> cartItems) {
        Set<CartItemResponse> responses = new HashSet<>();
        for (CartItem cartItem : cartItems) {
            CartItemResponse response = CartItemResponse.builder()
                    .productId(cartItem.getProduct().getId())
                    .productName(cartItem.getProduct().getName())
                    .priceAtPurchase(cartItem.getPriceAtPurchase())
                    .quantity(cartItem.getQuantity())
                    .totalPrice(cartItem.getPriceAtPurchase().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .build();
            responses.add(response);
        }
        return responses;
    }
}
