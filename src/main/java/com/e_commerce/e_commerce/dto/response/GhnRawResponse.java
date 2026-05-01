package com.e_commerce.e_commerce.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class GhnRawResponse<T> {
    int code;
    String message;
    T data;
}
