package com.e_commerce.e_commerce.entity;

import com.e_commerce.e_commerce.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @Column(unique = true, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci", nullable = false)
    String username;
    String firstName;
    String lastName;
    String password;
    String phoneNumber;
    String address;
    String email;
    @Enumerated(EnumType.STRING)
    Gender gender;
    LocalDate dob;
    @Builder.Default
    boolean active = true;
    int tokenVersion;
    @ManyToMany
    Set<Role> roles;
}
