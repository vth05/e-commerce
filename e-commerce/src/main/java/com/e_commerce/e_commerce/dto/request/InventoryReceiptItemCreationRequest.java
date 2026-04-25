package com.e_commerce.e_commerce.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryReceiptItemCreationRequest {
    @NotBlank(message = "PRODUCT_VARIANT_ID_REQUIRED")
    String productVariantId;

    @NotNull(message = "QUANTITY_REQUIRED")
    @Min(value = 1, message = "QUANTITY_INVALID")
    Long quantity;

    @NotNull(message = "IMPORT_PRICE_REQUIRED")
    @DecimalMin(value = "0", message = "IMPORT_PRICE_INVALID")
    BigDecimal importPrice;
}
