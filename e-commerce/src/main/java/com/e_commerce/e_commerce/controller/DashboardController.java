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
    ApiResponse<RevenueStatsResponse> getDailyRevenue(
            @RequestParam(required = false) CheckoutStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ApiResponse.<RevenueStatsResponse>builder()
                .result(dashboardService.getDailyRevenue(status, start, end))
                .build();
    }

    @GetMapping("/revenue/products")
    ApiResponse<List<ProductRevenueResponse>> getRevenueByProductAndDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ApiResponse.<List<ProductRevenueResponse>>builder()
                .result(dashboardService.getRevenueByProductAndDateRange(start, end))
                .build();
    }

    @GetMapping("/revenue/products/top-revenue")
    ApiResponse<Page<TopProductsByRevenueResponse>> getTopProductsByRevenueAndDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ApiResponse.<Page<TopProductsByRevenueResponse>>builder()
                .result(dashboardService.getTopProductsByRevenueAndDateRange(start, end, limit))
                .build();
    }

    @GetMapping("/revenue/products/top-quantity-sold")
    ApiResponse<Page<TopProductsByQuantitySoldResponse>> getTopProductsByQuantitySoldAndDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ApiResponse.<Page<TopProductsByQuantitySoldResponse>>builder()
                .result(dashboardService.getTopProductsByQuantitySoldAndDateRange(start, end, limit))
                .build();
    }
}
