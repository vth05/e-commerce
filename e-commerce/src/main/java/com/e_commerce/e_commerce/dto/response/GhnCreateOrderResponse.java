package com.e_commerce.e_commerce.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GhnCreateOrderResponse {
    String order_code;
    BigDecimal total_fee;
    Instant expected_delivery_time;
}
