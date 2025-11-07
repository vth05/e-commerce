package com.e_commerce.e_commerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    String id;
    String userId;
    String cartStatus;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    BigDecimal totalPrice;
    List<CartItemResponse> cartItems;
}
