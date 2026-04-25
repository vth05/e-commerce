package com.e_commerce.e_commerce.dto.response;

import java.math.BigDecimal;

public record TopSellingProductsResponse(
        String productId,
        String productName,
        BigDecimal price,
        Long stockQuantity,
        Long quantitySold
) {
}
