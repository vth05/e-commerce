package com.e_commerce.e_commerce.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class GhnOrderItem {
    String name;
    Integer quantity;
    Integer weight;
}
