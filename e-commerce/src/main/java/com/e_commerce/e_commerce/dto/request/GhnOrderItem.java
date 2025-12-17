package com.e_commerce.e_commerce.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GhnOrderItem {
    String name;
    Integer quantity;
    Integer weight;
}
