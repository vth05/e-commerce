package com.e_commerce.e_commerce.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GhnCancelOrderResponse {
    String order_code;
    Boolean result;
    String message;
}
