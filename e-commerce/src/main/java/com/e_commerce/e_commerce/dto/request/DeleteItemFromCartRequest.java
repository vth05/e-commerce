package com.e_commerce.e_commerce.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeleteItemFromCartRequest {
    @NotBlank(message = "PRODUCT_ID_REQUIRED")
    String productId;
    @NotNull(message = "PRICE_AT_PURCHASE_REQUIRED")
    @DecimalMin(value = "1", message = "PRICE_AT_PURCHASE_INVALID")
    BigDecimal priceAtPurchase;
}
