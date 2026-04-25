package com.e_commerce.e_commerce.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CheckoutPreviewResponse {
    BigDecimal subtotal;
    BigDecimal discount;
    BigDecimal shippingFee;
    BigDecimal shippingDiscount;
    BigDecimal totalPrice;
}
