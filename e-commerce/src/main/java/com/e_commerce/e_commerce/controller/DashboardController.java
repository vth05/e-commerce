package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.dto.response.ProductRevenueResponse;
import com.e_commerce.e_commerce.dto.response.RevenueStatsResponse;
import com.e_commerce.e_commerce.enums.CheckoutStatus;
import com.e_commerce.e_commerce.service.DashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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

    @GetMapping("/revenue")
    ApiResponse<RevenueStatsResponse> getRevenueStats(
            @RequestParam(required = false) CheckoutStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ApiResponse.<RevenueStatsResponse>builder()
                .result(dashboardService.getRevenueStats(status, start, end))
                .build();
    }

    @GetMapping("/revenue/products")
    ApiResponse<List<ProductRevenueResponse>> getRevenueByProduct(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ApiResponse.<List<ProductRevenueResponse>>builder()
                .result(dashboardService.getRevenueByProduct(start, end))
                .build();
    }
}
