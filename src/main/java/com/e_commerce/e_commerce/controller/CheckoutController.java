package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.request.CheckoutPreviewRequest;
import com.e_commerce.e_commerce.dto.request.CheckoutRequest;
import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.dto.response.CheckoutPreviewResponse;
import com.e_commerce.e_commerce.dto.response.OrderResponse;
import com.e_commerce.e_commerce.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CheckoutController {
    CheckoutService checkoutService;

    @PostMapping
    public ApiResponse<OrderResponse> checkout(@RequestBody @Valid CheckoutRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .result(checkoutService.checkout(request))
                .build();
    }

    @PostMapping("/preview")
    public ApiResponse<CheckoutPreviewResponse> checkoutPreview(@RequestBody CheckoutPreviewRequest request) {
        return ApiResponse.<CheckoutPreviewResponse>builder()
                .result(checkoutService.checkoutPreview(request))
                .build();
    }
}
