package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.request.ProductCreationRequest;
import com.e_commerce.e_commerce.dto.request.ProductUpdateRequest;
import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.dto.response.ProductResponse;
import com.e_commerce.e_commerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
    ApiResponse<List<ProductResponse>> getProducts() {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.getProducts())
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
    ApiResponse<ProductResponse> deleteProduct(@PathVariable String productId) {
        return ApiResponse.<ProductResponse>builder()
                .message("Product deleted successfully")
                .result(productService.deactivateProduct(productId))
                .build();
    }
}
