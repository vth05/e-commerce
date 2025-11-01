package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.request.AddItemToCartRequest;
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
    ApiResponse<CartResponse> addItemToCart(@RequestBody @Valid AddItemToCartRequest addItemToCartRequest) {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.addItemToCart(addItemToCartRequest))
                .build();
    }

    @DeleteMapping("/{cartItemId}")
    ApiResponse<CartResponse> deleteItemFromCart(@PathVariable String cartItemId) {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.deleteItemFromCart(cartItemId))
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
