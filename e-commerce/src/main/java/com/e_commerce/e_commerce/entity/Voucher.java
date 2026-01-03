package com.e_commerce.e_commerce.entity;

import com.e_commerce.e_commerce.enums.Category;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    Integer discountAmount;

    Integer discountPercent;

    BigDecimal minOrderValue;

    BigDecimal maxDiscountAmount;

    LocalDateTime validFrom;

    LocalDateTime validTo;

    String description;

    Integer usageLimit;

    @Enumerated(EnumType.STRING)
    Category category;

    @Builder.Default
    Integer usageCount = 0;

    @Builder.Default
    boolean active = true;
}
