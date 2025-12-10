package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.service.ProductVariantImageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/product-variants")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ProductVariantImageController {
    ProductVariantImageService productVariantImageService;

//    @PostMapping("/{variantId}/images")
//    ApiResponse<String> uploadImage(
//            @PathVariable String variantId,
//            @RequestParam MultipartFile file
//    ) throws IOException {
//        return ApiResponse.<String>builder()
//                .result(productVariantImageService.uploadImage(file))
//                .build();
//    }
}
