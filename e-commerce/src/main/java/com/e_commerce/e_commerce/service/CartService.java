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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class CartService {
//    CartRepository cartRepository;
//    ProductRepository productRepository;
//    CartItemRepository cartItemRepository;
//
//    @Transactional
//    public CartResponse addItemToCart(AddItemToCartRequest addItemToCartRequest) {
//        Product product = productRepository.findById(addItemToCartRequest.getProductId()).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
//        long quantityFromRequest = addItemToCartRequest.getQuantity();
//        if (product.getQuantity() < quantityFromRequest) {
//            throw new AppException(ErrorCode.PRODUCT_INSUFFICIENT_STOCK);
//        }
//
//        String userId = getUserIdFromAuthentication();
//        Cart cart = cartRepository.findByUserIdAndCartStatus(userId, CartStatus.ACTIVE).orElseGet(() -> {
//            Cart newCart = Cart.builder()
//                    .userId(userId)
//                    .cartStatus(CartStatus.ACTIVE)
//                    // avoid NullPointerException when adding cartItem
//                    .cartItems(new HashSet<>())
//                    // avoid NullPointerException when calculating totalPrice
//                    .totalPrice(BigDecimal.ZERO)
//                    .build();
//            // create cartId to find cartItem later
//            return cartRepository.save(newCart);
//        });
//
//        BigDecimal productCurrentPrice = product.getPrice();
//        CartItem cartItem = cartItemRepository.findByCartAndProductAndPriceAtPurchaseAndActive(cart, product, productCurrentPrice, true).orElseGet(() -> {
//            CartItem newCartItem = CartItem.builder()
//                    .cart(cart)
//                    .product(product)
//                    .priceAtPurchase(productCurrentPrice)
//                    .quantity(0)
//                    .build();
//            cart.getCartItems().add(newCartItem);
//            return newCartItem;
//        });
//
//        // update quantity of cartItem
//        cartItem.setQuantity(cartItem.getQuantity() + quantityFromRequest);
//        cartItemRepository.save(cartItem);
//
//        // update totalPrice of cart
//        BigDecimal totalPriceFromRequest = productCurrentPrice.multiply(BigDecimal.valueOf(quantityFromRequest));
//        cart.setTotalPrice(cart.getTotalPrice().add(totalPriceFromRequest));
//        cartRepository.save(cart);
//
//        Set<CartItem> cartItems = filterActiveCartItems(cart.getCartItems());
//        return CartResponse.builder()
//                .id(cart.getId())
//                .userId(userId)
//                .cartStatus(String.valueOf(cart.getCartStatus()))
//                .totalPrice(cart.getTotalPrice())
//                .createdAt(cart.getCreatedAt())
//                .updatedAt(cart.getUpdatedAt())
//                .cartItems(cartItemSetToCartItemResponseSet(cartItems))
//                .build();
//    }
//
//    @Transactional
//    public CartResponse deleteItemFromCart(String cartItemId) {
//        String userId = getUserIdFromAuthentication();
//        Cart cart = cartRepository.findByUserIdAndCartStatus(userId, CartStatus.ACTIVE).orElseThrow(() -> new AppException(ErrorCode.CART_NOT_EXISTED));
//        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_EXISTED));
//        if (!cartItem.getCart().getId().equals(cart.getId())) {
//            throw new AppException(ErrorCode.UNAUTHENTICATED);
//        }
//
//        // update totalPrice of cart
//        long cartItemQuantity = cartItem.getQuantity();
//        BigDecimal totalPriceReduction = cartItem.getPriceAtPurchase().multiply(BigDecimal.valueOf(cartItemQuantity));
//        cart.setTotalPrice(cart.getTotalPrice().subtract(totalPriceReduction));
//        cartRepository.save(cart);
//
//        // soft delete cartItem
//        cartItem.setActive(false);
//        cartItemRepository.save(cartItem);
//
//        Set<CartItem> cartItems = filterActiveCartItems(cart.getCartItems());
//        return CartResponse.builder()
//                .id(cart.getId())
//                .userId(userId)
//                .cartStatus(String.valueOf(cart.getCartStatus()))
//                .totalPrice(cart.getTotalPrice())
//                .createdAt(cart.getCreatedAt())
//                .updatedAt(cart.getUpdatedAt())
//                .cartItems(cartItemSetToCartItemResponseSet(cartItems))
//                .build();
//    }
//
//    @Transactional
//    public void deleteCart() {
//        String userId = getUserIdFromAuthentication();
//        Cart cart = cartRepository.findByUserIdAndCartStatus(userId, CartStatus.ACTIVE).orElseThrow(() -> new AppException(ErrorCode.CART_NOT_EXISTED));
//        Set<CartItem> cartItems = cart.getCartItems();
//        cart.setCartStatus(CartStatus.CANCELED);
//        cart.setTotalPrice(BigDecimal.ZERO);
//        cartRepository.save(cart);
//
//        // update quantity of products
//        for (CartItem cartItem : cartItems) {
//            cartItem.setActive(false);
//            cartItemRepository.save(cartItem);
//        }
//    }
//
//    public CartResponse getCurrentCart() {
//        String userId = getUserIdFromAuthentication();
//        Cart cart = cartRepository.findByUserIdAndCartStatus(userId, CartStatus.ACTIVE).orElseThrow(() -> new AppException(ErrorCode.CART_NOT_EXISTED));
//        Set<CartItem> cartItems = filterActiveCartItems(cart.getCartItems());
//        return CartResponse.builder()
//                .id(cart.getId())
//                .userId(userId)
//                .cartStatus(String.valueOf(cart.getCartStatus()))
//                .totalPrice(cart.getTotalPrice())
//                .createdAt(cart.getCreatedAt())
//                .updatedAt(cart.getUpdatedAt())
//                .cartItems(cartItemSetToCartItemResponseSet(cartItems))
//                .build();
//    }
//
//    private String getUserIdFromAuthentication() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        Jwt jwt = (Jwt) authentication.getPrincipal();
//        return jwt.getClaimAsString("userId");
//    }
//
//    private Set<CartItem> filterActiveCartItems(Set<CartItem> cartItems) {
//        return cartItems.stream().filter(ci -> ci.isActive()).collect(Collectors.toSet());
//    }
//
//    private Set<CartItemResponse> cartItemSetToCartItemResponseSet(Set<CartItem> cartItems) {
//        Set<CartItemResponse> responses = new HashSet<>();
//        for (CartItem cartItem : cartItems) {
//            CartItemResponse response = CartItemResponse.builder()
//                    .id(cartItem.getId())
//                    .productId(cartItem.getProduct().getId())
//                    .productName(cartItem.getProduct().getName())
//                    .priceAtPurchase(cartItem.getPriceAtPurchase())
//                    .quantity(cartItem.getQuantity())
//                    .totalPrice(cartItem.getPriceAtPurchase().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
//                    .active(cartItem.isActive())
//                    .build();
//            responses.add(response);
//        }
//        return responses;
//    }
}
