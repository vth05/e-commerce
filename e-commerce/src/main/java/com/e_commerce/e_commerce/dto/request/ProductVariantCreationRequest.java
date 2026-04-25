package com.e_commerce.e_commerce.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class ProductVariantCreationRequest {
    @NotNull(message = "PRODUCT_VARIANT_PRICE_REQUIRED")
    @DecimalMin(value = "0", message = "PRODUCT_VARIANT_PRICE_INVALID")
    BigDecimal price;

    @DecimalMin(value = "0", message = "PRODUCT_VARIANT_WEIGHT_INVALID")
    BigDecimal weight;

    @DecimalMin(value = "0", message = "PRODUCT_VARIANT_LENGTH_INVALID")
    BigDecimal length;

    @DecimalMin(value = "0", message = "PRODUCT_VARIANT_WIDTH_INVALID")
    BigDecimal width;

    @DecimalMin(value = "0", message = "PRODUCT_VARIANT_HEIGHT_INVALID")
    BigDecimal height;

    @NotNull(message = "PRODUCT_VARIANT_QUANTITY_REQUIRED")
    @Min(value = 0, message = "PRODUCT_VARIANT_QUANTITY_INVALID")
    Long quantity;

    String color;

    String ram;

    String storage;

    String cpu;

    String gpu;

    BigDecimal screenSize;

    String screenResolution;

    Integer refreshRateHz;
}
