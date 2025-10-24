package com.e_commerce.e_commerce.entity;

import com.e_commerce.e_commerce.enums.Category;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

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
    // save enum as string in database
    @Enumerated(EnumType.STRING)
    Category category;
    BigDecimal price;
    long quantity;
    String description;
    boolean active;
    @OneToMany(mappedBy = "product")
    Set<CartItem> cartItems;
}
