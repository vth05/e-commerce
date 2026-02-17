package com.e_commerce.e_commerce.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupplierCreationRequest {
    @NotBlank(message = "SUPPLIER_NAME_REQUIRED")
    @Size(max = 255, message = "SUPPLIER_NAME_TOO_LONG")
    String name;

    @NotBlank(message = "SUPPLIER_PHONE_REQUIRED")
    @Pattern(regexp = "^[0-9]{8,15}$", message = "PHONE_INVALID")
    String phone;

    @NotBlank(message = "SUPPLIER_EMAIL_REQUIRED")
    @Email(message = "EMAIL_INVALID", regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    String email;

    @NotBlank(message = "SUPPLIER_ADDRESS_REQUIRED")
    @Size(max = 255, message = "SUPPLIER_ADDRESS_TOO_LONG")
    String address;
}
