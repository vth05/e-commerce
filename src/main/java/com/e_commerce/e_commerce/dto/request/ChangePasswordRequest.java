package com.e_commerce.e_commerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequest {
    @NotBlank(message = "OLD_PASSWORD_REQUIRED")
    @Size(min = 6, message = "PASSWORD_INVALID")
    String oldPassword;
    @NotBlank(message = "NEW_PASSWORD_REQUIRED")
    @Size(min = 6, message = "PASSWORD_INVALID")
    String newPassword;

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword == null ? null : oldPassword.trim();
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword == null ? null : newPassword.trim();
    }
}
