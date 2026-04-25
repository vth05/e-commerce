package com.e_commerce.e_commerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupplierResponse {
    String id;

    String name;

    String phone;

    String email;

    String address;

    Boolean active;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
