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
import org.springframework.data.domain.Page;
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

    @GetMapping("/history")
    public ApiResponse<Page<OrderResponse>> getOrderHistoryOfCurrentUser(
            @RequestParam(required = false) CheckoutStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ApiResponse.<Page<OrderResponse>>builder()
                .result(checkoutService.getOrderHistoryOfCurrentUser(status, page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<OrderResponse>> listOrdersForAdmin(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) CheckoutStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ApiResponse.<Page<OrderResponse>>builder()
                .result(checkoutService.listOrdersForAdmin(userId, status, page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable String orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(checkoutService.getOrderById(orderId))
                .build();
    }
}
