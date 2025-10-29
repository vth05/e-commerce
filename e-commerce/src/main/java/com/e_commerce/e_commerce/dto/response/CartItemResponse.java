package com.e_commerce.e_commerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    String productId;
    String productName;
    BigDecimal priceAtPurchase;
    long quantity;
    BigDecimal totalPrice;
}
