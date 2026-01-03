package com.e_commerce.e_commerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    String id;

    String productName;

    String productId;

    String productVariantId;

    BigDecimal priceAtPurchase;

    long quantity;

    String voucherCode;

    BigDecimal discountAmount;
}
