package com.e_commerce.e_commerce.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
    @Size(min = 4, message = "USERNAME_INVALID")
    String username;
    String firstName;
    String lastName;
    @Size(min = 6, message = "PASSWORD_INVALID")
    String password;
    String phoneNumber;
    String address;
    @Email(message = "EMAIL_INVALID", regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    String email;
    LocalDate dob;
    String gender;
    Boolean active;

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName == null ? null : firstName.trim();
    }

    public void setLastName(String lastName) {
        this.lastName = lastName == null ? null : lastName.trim();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber == null ? null : phoneNumber.trim();
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public void setGender(String gender) {
        this.gender = gender == null ? null : gender.trim();
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }
}
