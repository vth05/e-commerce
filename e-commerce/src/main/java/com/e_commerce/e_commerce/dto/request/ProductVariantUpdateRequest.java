package com.e_commerce.e_commerce.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
public class ProductVariantUpdateRequest {
    @DecimalMin(value = "0", message = "PRODUCT_VARIANT_PRICE_INVALID")
    BigDecimal price;

    @Min(value = 0, message = "PRODUCT_VARIANT_QUANTITY_INVALID")
    Long quantity;

    String color;

    String ram;

    String storage;

    Boolean active;
}
