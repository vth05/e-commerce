package com.e_commerce.e_commerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantResponse {
    String id;

    String productVariantName;

    BigDecimal price;

    BigDecimal weight;

    BigDecimal length;

    BigDecimal width;

    BigDecimal height;

    long quantity;

    String color;

    String ram;

    String storage;

    String cpu;

    String gpu;

    BigDecimal screenSize;

    String screenResolution;

    Integer refreshRateHz;

    String sku;

    boolean active;

    String productId;

    List<ProductImageResponse> productImages;
}
