package com.e_commerce.e_commerce.entity;

import com.e_commerce.e_commerce.enums.CartStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String userId;
    @Enumerated(EnumType.STRING)
    CartStatus cartStatus;
    double totalPrice;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
