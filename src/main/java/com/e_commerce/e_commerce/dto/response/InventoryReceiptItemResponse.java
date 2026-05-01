package com.e_commerce.e_commerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryReceiptItemResponse {
    String id;

    String inventoryReceiptId;

    String productVariantId;

    Long quantity;

    BigDecimal importPrice;

    BigDecimal totalPrice;
}
