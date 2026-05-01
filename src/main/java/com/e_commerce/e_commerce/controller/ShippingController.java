package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.request.*;
import com.e_commerce.e_commerce.dto.response.*;
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

    @PostMapping("/expected-delivery-time")
    ApiResponse<GhnExpectedDeliveryTimeResponse> calculateExpectedDeliveryTime(@RequestBody GhnExpectedDeliveryTimeRequest request) {
        return ApiResponse.<GhnExpectedDeliveryTimeResponse>builder()
                .code(200)
                .message("Success")
                .result(ghnService.calculateExpectedDeliveryTime(request))
                .build();
    }

    @PostMapping("/return-order")
    ApiResponse<List<GhnReturnOrderResponse>> returnOrder(@RequestBody GhnReturnOrderRequest request) {
        return ApiResponse.<List<GhnReturnOrderResponse>>builder()
                .code(200)
                .message("Success")
                .result(ghnService.returnOrder(request))
                .build();
    }

    @PostMapping("/order-info")
    ApiResponse<GhnOrderInfoResponse> getOrderInfo(@RequestBody GhnOrderInfoRequest request) {
        return ApiResponse.<GhnOrderInfoResponse>builder()
                .code(200)
                .message("Success")
                .result(ghnService.getOrderInfo(request))
                .build();
    }
}
