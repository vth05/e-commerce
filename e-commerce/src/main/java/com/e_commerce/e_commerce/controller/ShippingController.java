package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.request.GhnCalculateFeeRequest;
import com.e_commerce.e_commerce.dto.request.GhnCancelOrderRequest;
import com.e_commerce.e_commerce.dto.request.GhnCreateOrderRequest;
import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.dto.response.GhnCalculateFeeResponse;
import com.e_commerce.e_commerce.dto.response.GhnCancelOrderResponse;
import com.e_commerce.e_commerce.dto.response.GhnCreateOrderResponse;
import com.e_commerce.e_commerce.service.GhnService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shipping")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ShippingController {
    GhnService ghnService;

    @PostMapping("/fee")
    ApiResponse<GhnCalculateFeeResponse> calculateFee(@RequestBody GhnCalculateFeeRequest request) {
        return ApiResponse.<GhnCalculateFeeResponse>builder()
                .code(200)
                .message("Success")
                .result(ghnService.calculateFee(request))
                .build();
    }

    @PostMapping("/order")
    ApiResponse<GhnCreateOrderResponse> createOrder(@RequestBody GhnCreateOrderRequest request) {
        return ApiResponse.<GhnCreateOrderResponse>builder()
                .code(200)
                .message("Success")
                .result(ghnService.createOrder(request))
                .build();
    }

    @PostMapping("/cancel")
    ApiResponse<List<GhnCancelOrderResponse>> cancelOrders(@RequestBody GhnCancelOrderRequest request) {
        return ApiResponse.<List<GhnCancelOrderResponse>>builder()
                .code(200)
                .message("Success")
                .result(ghnService.cancelOrders(request))
                .build();
    }
}
