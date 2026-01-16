package com.e_commerce.e_commerce.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ChatbotProductResponse(
        String id,
        String name,
        String brand,
        String category,
        String description,
        List<ChatbotVariantResponse> productVariants
) {
}
