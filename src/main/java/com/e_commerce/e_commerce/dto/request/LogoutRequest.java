package com.e_commerce.e_commerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class LogoutRequest {
    @NotBlank(message = "TOKEN_REQUIRED")
    String token;
}
