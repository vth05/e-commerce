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
    private String id;

    private BigDecimal price;

    private long quantity;

    private String color;

    private String ram;

    private String storage;

    private String sku;

    private boolean active;

    private String productId;

    private List<String> productImages;
}
