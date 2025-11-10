package com.e_commerce.e_commerce.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String code;

    BigDecimal discountAmount;

    BigDecimal discountPercent;

    LocalDateTime validFrom;

    LocalDateTime validTo;

    String description;

    Integer usageLimit;

    @Builder.Default
    Integer usageCount = 0;

    @Builder.Default
    boolean active = true;

    @OneToMany(mappedBy = "voucher")
    @Builder.Default
    List<Order> orders = new ArrayList<>();
}
