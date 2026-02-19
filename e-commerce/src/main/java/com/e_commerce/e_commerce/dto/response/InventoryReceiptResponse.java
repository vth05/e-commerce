package com.e_commerce.e_commerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryReceiptResponse {
    String id;

    BigDecimal totalPrice;

    String note;

    String status;

    String supplierId;

    String supplierName;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
