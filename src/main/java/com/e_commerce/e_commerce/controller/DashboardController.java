package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.response.*;
import com.e_commerce.e_commerce.enums.CheckoutStatus;
import com.e_commerce.e_commerce.service.DashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DashboardController {
    DashboardService dashboardService;

    @GetMapping("/revenue/daily")
    ApiResponse<RevenueStatsResponse> getDailyRevenueByDateRangeAndCheckoutStatus(
            @RequestParam(required = false) CheckoutStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ApiResponse.<RevenueStatsResponse>builder()
                .result(dashboardService.getDailyRevenueByDateRangeAndCheckoutStatus(status, start, end))
                .build();
    }

    @GetMapping("/revenue/products")
    ApiResponse<Page<ProductRevenueResponse>> getRevenueByProductAndDateRangeAndCheckoutStatus(
            @RequestParam(required = false) CheckoutStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ApiResponse.<Page<ProductRevenueResponse>>builder()
                .result(dashboardService.getRevenueByProductAndDateRangeAndCheckoutStatus(status, start, end, page, limit))
                .build();
    }

    @GetMapping("/revenue/products/top-revenue")
    ApiResponse<Page<TopProductsByRevenueResponse>> getTopProductsByRevenueAndDateRangeAndCheckoutStatus(
            @RequestParam(required = false) CheckoutStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ApiResponse.<Page<TopProductsByRevenueResponse>>builder()
                .result(dashboardService.getTopProductsByRevenueAndDateRangeAndCheckoutStatus(status, start, end, page, limit))
                .build();
    }

    @GetMapping("/revenue/products/top-quantity-sold")
    ApiResponse<Page<TopProductsByQuantitySoldResponse>> getTopProductsByQuantitySoldAndDateRangeAndCheckoutStatus(
            @RequestParam(required = false) CheckoutStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ApiResponse.<Page<TopProductsByQuantitySoldResponse>>builder()
                .result(dashboardService.getTopProductsByQuantitySoldAndDateRangeAndCheckoutStatus(status, start, end, page, limit))
                .build();
    }

    @GetMapping("/revenue/users/top-revenue")
    ApiResponse<List<TopCustomersResponse>> getTopCustomersByUserStatusAndDateRangeAndCheckoutStatus(
            @RequestParam(required = false) CheckoutStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ApiResponse.<List<TopCustomersResponse>>builder()
                .result(dashboardService.getTopCustomersByUserStatusAndDateRangeAndCheckoutStatus(status, start, end, active, page, limit))
                .build();
    }

    @GetMapping("/product-variants/low-stock")
    public ApiResponse<Page<ProductVariantLowStockResponse>> getProductVariantsLowInStock(
            @RequestParam(defaultValue = "10") int threshold,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ApiResponse.<Page<ProductVariantLowStockResponse>>builder()
                .result(dashboardService.getProductVariantsLowInStock(threshold, page, limit))
                .build();
    }
}
