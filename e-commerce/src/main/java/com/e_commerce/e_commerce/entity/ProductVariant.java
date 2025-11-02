package com.e_commerce.e_commerce.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    BigDecimal price;

    long quantity;

    String color;

    String ram;

    String storage;

    @Column(unique = true, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci", nullable = false)
    String sku;

    @Builder.Default
    boolean active = true;

    @ManyToOne
    @JoinColumn(name = "product_id")
    Product product;

    @OneToMany(mappedBy = "productVariant")
    List<CartItem> cartItems = new ArrayList<>();

    @OneToMany(mappedBy = "productVariant")
    List<ProductImage> productImages = new ArrayList<>();
}
