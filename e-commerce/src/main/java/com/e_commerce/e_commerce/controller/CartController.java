package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.request.AddItemToCartRequest;
import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.dto.response.CartResponse;
import com.e_commerce.e_commerce.service.CartService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
