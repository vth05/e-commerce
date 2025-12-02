package com.e_commerce.e_commerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherResponse {
    String id;

    String code;

    Integer discountAmount;

    Integer discountPercent;

    LocalDateTime validFrom;

    LocalDateTime validTo;

    String description;

    Integer usageLimit;

    String category;

    Integer usageCount;

    boolean active;
}
