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
public class OrderResponse {
    String id;

    String userId;

    String checkoutStatus;

    String receiverName;

    String receiverPhone;

    String shippingAddress;

    String paymentMethod;

    BigDecimal subtotal;

    BigDecimal shippingFee;

    BigDecimal discount;

    BigDecimal totalPrice;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    List<OrderItemResponse> orderItems;
}
