package com.e_commerce.e_commerce.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddProductVariantToCartRequest {
    @NotBlank(message = "PRODUCT_VARIANT_ID_REQUIRED")
    String productVariantId;
    @NotNull(message = "PRODUCT_VARIANT_QUANTITY_REQUIRED")
    @Min(value = 0, message = "PRODUCT_VARIANT_QUANTITY_INVALID")
    Long quantity;
}
