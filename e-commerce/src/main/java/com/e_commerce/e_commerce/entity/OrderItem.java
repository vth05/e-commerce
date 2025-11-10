package com.e_commerce.e_commerce.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String productName;

    String productId;

    String productVariantId;

    BigDecimal priceAtPurchase;

    long quantity;

    @Builder.Default
    boolean active = true;

    @ManyToOne
    @JoinColumn(name = "order_id")
    Order order;
}
