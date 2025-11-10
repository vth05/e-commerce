package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.request.CheckoutRequest;
import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.dto.response.OrderResponse;
import com.e_commerce.e_commerce.enums.CheckoutStatus;
import com.e_commerce.e_commerce.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/history")
    public ApiResponse<List<OrderResponse>> getOrderHistoryOfCurrentUser(@RequestParam(required = false) CheckoutStatus status) {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(checkoutService.getOrderHistoryOfCurrentUser(status))
                .build();
    }

    @GetMapping
    public ApiResponse<List<OrderResponse>> listOrdersForAdmin(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) CheckoutStatus status
    ) {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(checkoutService.listOrdersForAdmin(userId, status))
                .build();
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable String orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(checkoutService.getOrderById(orderId))
                .build();
    }
}
