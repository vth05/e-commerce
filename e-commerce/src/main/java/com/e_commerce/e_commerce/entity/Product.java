package com.e_commerce.e_commerce.entity;

import com.e_commerce.e_commerce.enums.Category;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String name;
    double price;
    long quantity;
    // save enum as string in database
    @Enumerated(EnumType.STRING)
    Category category;
    String description;
    boolean active;
}
