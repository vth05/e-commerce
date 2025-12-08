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
    String phoneNumber;
    String address;
    String gender;
    LocalDate dob;
    Boolean active;
    Boolean emailVerified;

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
}
