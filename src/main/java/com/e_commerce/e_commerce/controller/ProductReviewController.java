package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.request.ProductReviewCreationRequest;
import com.e_commerce.e_commerce.dto.request.ProductReviewUpdateRequest;
import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.dto.response.ProductReviewResponse;
import com.e_commerce.e_commerce.dto.response.ProductReviewSummaryResponse;
import com.e_commerce.e_commerce.service.ProductReviewService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product-reviews")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductReviewController {
    ProductReviewService productReviewService;

    @PostMapping
    ApiResponse<ProductReviewResponse> createProductReview(@RequestBody @Valid ProductReviewCreationRequest request) {
        return ApiResponse.<ProductReviewResponse>builder()
                .result(productReviewService.createProductReview(request))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<ProductReviewResponse> updateProductReview(@PathVariable String id, @RequestBody @Valid ProductReviewUpdateRequest request) {
        return ApiResponse.<ProductReviewResponse>builder()
                .result(productReviewService.updateProductReview(id, request))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<ProductReviewResponse> getProductReview(@PathVariable String id) {
        return ApiResponse.<ProductReviewResponse>builder()
                .result(productReviewService.getProductReview(id))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<ProductReviewResponse> deleteProductReview(@PathVariable String id) {
        return ApiResponse.<ProductReviewResponse>builder()
                .result(productReviewService.deleteProductReview(id))
                .build();
    }

    @PutMapping("/{id}/approve")
    ApiResponse<ProductReviewResponse> approveProductReview(@PathVariable String id) {
        return ApiResponse.<ProductReviewResponse>builder()
                .result(productReviewService.approveProductReview(id))
                .build();
    }

    @PutMapping("/{id}/reject")
    ApiResponse<ProductReviewResponse> rejectProductReview(@PathVariable String id, @RequestParam String reason) {
        return ApiResponse.<ProductReviewResponse>builder()
                .result(productReviewService.rejectProductReview(id, reason))
                .build();
    }

    @GetMapping
    ApiResponse<Page<ProductReviewResponse>> getProductReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ApiResponse.<Page<ProductReviewResponse>>builder()
                .result(productReviewService.getProductReviews(page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/pending")
    ApiResponse<Page<ProductReviewResponse>> getPendingProductReviewsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ApiResponse.<Page<ProductReviewResponse>>builder()
                .result(productReviewService.getPendingProductReviewsForAdmin(page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/product/{productId}")
    ApiResponse<Page<ProductReviewResponse>> getProductReviewsByProductId(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ApiResponse.<Page<ProductReviewResponse>>builder()
                .result(productReviewService.getProductReviewsByProductId(productId, page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/product/{productId}/summary")
    ApiResponse<ProductReviewSummaryResponse> getProductReviewMetadataByProductId(@PathVariable String productId) {
        return ApiResponse.<ProductReviewSummaryResponse>builder()
                .result(productReviewService.getProductReviewMetadataByProductId(productId))
                .build();
    }
}
