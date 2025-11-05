package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.request.ProductVariantCreationRequest;
import com.e_commerce.e_commerce.dto.request.ProductVariantUpdateRequest;
import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.dto.response.ProductVariantResponse;
import com.e_commerce.e_commerce.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-variants")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductVariantController {
    ProductVariantService productVariantService;

    @PostMapping("/{productId}")
    public ApiResponse<ProductVariantResponse> createProductVariant(@RequestBody @Valid ProductVariantCreationRequest request, @PathVariable String productId) {
        return ApiResponse.<ProductVariantResponse>builder()
                .result(productVariantService.createProductVariant(request, productId))
                .build();
    }

    @GetMapping("/{productVariantId}")
    public ApiResponse<ProductVariantResponse> getProductVariantById(@PathVariable String productVariantId) {
        return ApiResponse.<ProductVariantResponse>builder()
                .result(productVariantService.getProductVariantById(productVariantId))
                .build();
    }

    @GetMapping("/product/{productId}")
    public ApiResponse<List<ProductVariantResponse>> getProductVariantsByProductId(@PathVariable String productId) {
        return ApiResponse.<List<ProductVariantResponse>>builder()
                .result(productVariantService.getProductVariantsByProductId(productId))
                .build();
    }

    @PutMapping("/{productVariantId}")
    public ApiResponse<ProductVariantResponse> updateProductVariant(@RequestBody @Valid ProductVariantUpdateRequest request, @PathVariable String productVariantId) {
        return ApiResponse.<ProductVariantResponse>builder()
                .result(productVariantService.updateProductVariant(request, productVariantId))
                .build();
    }

    @DeleteMapping("/{productVariantId}")
    public ApiResponse<ProductVariantResponse> deactivateProductVariant(@PathVariable String productVariantId) {
        return ApiResponse.<ProductVariantResponse>builder()
                .message("Product variant deleted successfully")
                .result(productVariantService.deactivateProductVariant(productVariantId))
                .build();
    }
}
