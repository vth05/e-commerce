package com.e_commerce.e_commerce.entity;

import com.e_commerce.e_commerce.enums.Category;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

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

    String description;

    @Builder.Default
    boolean active = true;

    @OneToMany(mappedBy = "product")
    List<ProductVariant> productVariants = new ArrayList<>();
}
