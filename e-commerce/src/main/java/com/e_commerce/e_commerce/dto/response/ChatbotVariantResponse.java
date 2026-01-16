package com.e_commerce.e_commerce.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ChatbotVariantResponse(
        String productVariantName,
        BigDecimal price,
        long quantity,
        String ram,
        String storage,
        String cpu,
        String gpu,
        BigDecimal screenSize,
        String screenResolution,
        Integer refreshRateHz
) {
}
