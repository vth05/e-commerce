package com.e_commerce.e_commerce.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupplierUpdateRequest {
    @Size(max = 255, message = "SUPPLIER_NAME_TOO_LONG")
    String name;

    @Pattern(regexp = "^[0-9]{8,15}$", message = "PHONE_INVALID")
    String phone;

    @Email(message = "EMAIL_INVALID", regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    String email;

    @Size(max = 255, message = "SUPPLIER_ADDRESS_TOO_LONG")
    String address;

    Boolean active;
}
