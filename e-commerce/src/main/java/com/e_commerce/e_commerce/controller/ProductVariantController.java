package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.request.ProductVariantCreationRequest;
import com.e_commerce.e_commerce.dto.request.ProductVariantUpdateRequest;
import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.dto.response.ProductImageResponse;
import com.e_commerce.e_commerce.dto.response.ProductVariantResponse;
import com.e_commerce.e_commerce.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
    public ApiResponse<Page<ProductVariantResponse>> getProductVariantsByProductId(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "productVariantName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ApiResponse.<Page<ProductVariantResponse>>builder()
                .result(productVariantService.getProductVariantsByProductId(productId, page, size, sortBy, sortDir))
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

    @PostMapping(value = "/{productVariantId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<ProductImageResponse> uploadVariantImage(
            @PathVariable String productVariantId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return ApiResponse.<ProductImageResponse>builder()
                .result(productVariantService.uploadVariantImage(productVariantId, file))
                .build();
    }
}
