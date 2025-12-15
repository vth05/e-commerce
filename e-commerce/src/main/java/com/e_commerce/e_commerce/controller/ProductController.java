package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.request.ProductCreationRequest;
import com.e_commerce.e_commerce.dto.request.ProductSearchRequest;
import com.e_commerce.e_commerce.dto.request.ProductUpdateRequest;
import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.dto.response.ProductResponse;
import com.e_commerce.e_commerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductController {
    ProductService productService;

    @PostMapping
    ApiResponse<ProductResponse> createProduct(@RequestBody @Valid ProductCreationRequest productCreationRequest) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.createProduct(productCreationRequest))
                .build();
    }

    @GetMapping
    ApiResponse<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ApiResponse.<Page<ProductResponse>>builder()
                .result(productService.getProducts(page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/{productId}")
    ApiResponse<ProductResponse> getProduct(@PathVariable String productId) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getProduct(productId))
                .build();
    }

    @PutMapping("/{productId}")
    ApiResponse<ProductResponse> updateProduct(@PathVariable String productId, @RequestBody @Valid ProductUpdateRequest productUpdateRequest) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.updateProduct(productId, productUpdateRequest))
                .build();
    }

    @DeleteMapping("/{productId}")
    ApiResponse<ProductResponse> deactivateProduct(@PathVariable String productId) {
        return ApiResponse.<ProductResponse>builder()
                .message("Product deleted successfully")
                .result(productService.deactivateProduct(productId))
                .build();
    }

    @GetMapping("/search")
    ApiResponse<List<ProductResponse>> findBySearchCriteria(@RequestBody ProductSearchRequest request) {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.findBySearchCriteria(request))
                .build();
    }
}
