package com.e_commerce.e_commerce.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GhnReturnOrderRequest {
    List<String> order_codes;
}
