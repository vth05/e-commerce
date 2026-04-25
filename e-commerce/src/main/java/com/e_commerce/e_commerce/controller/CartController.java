package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.request.AddProductVariantToCartRequest;
import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.dto.response.CartResponse;
import com.e_commerce.e_commerce.service.CartService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CartController {
    CartService cartService;

    @PostMapping
    ApiResponse<CartResponse> addCartItemToCart(@RequestBody @Valid AddProductVariantToCartRequest request) {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.addCartItemToCart(request))
                .build();
    }

    @DeleteMapping("/{cartItemId}")
    ApiResponse<CartResponse> deleteCartItemFromCart(@PathVariable String cartItemId) {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.deleteCartItemFromCart(cartItemId))
                .build();
    }

    @DeleteMapping
    ApiResponse<Void> deleteCart() {
        cartService.deleteCart();
        return ApiResponse.<Void>builder()
                .message("Delete cart successfully")
                .build();
    }

    @GetMapping("/me")
    ApiResponse<CartResponse> getCurrentCart() {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.getCurrentCart())
                .build();
    }
}
