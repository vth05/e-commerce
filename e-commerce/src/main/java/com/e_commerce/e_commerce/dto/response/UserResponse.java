package com.e_commerce.e_commerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    String id;
    String username;
    String firstName;
    String lastName;
    String phoneNumber;
    String address;
    String email;
    String gender;
    LocalDate dob;
    boolean active;
    int tokenVersion;
    Set<RoleResponse> roles;
}
