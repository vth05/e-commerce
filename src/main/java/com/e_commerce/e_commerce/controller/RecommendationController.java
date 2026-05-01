package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.dto.response.TopSellingProductsResponse;
import com.e_commerce.e_commerce.service.RecommendationService;
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
@RequestMapping("/recommendation")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RecommendationController {
    RecommendationService recommendationService;

    @GetMapping("/products/top-quantity-sold")
    ApiResponse<List<TopSellingProductsResponse>> getTopSellingProductsForRecommendationByQuantitySoldAndDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ApiResponse.<List<TopSellingProductsResponse>>builder()
                .result(recommendationService.getTopSellingProductsForRecommendationByQuantitySoldAndDateRange(start, end, limit))
                .build();
    }
}
