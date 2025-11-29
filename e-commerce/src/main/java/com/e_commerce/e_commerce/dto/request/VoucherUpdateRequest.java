package com.e_commerce.e_commerce.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class VoucherUpdateRequest {
    String code;

    @DecimalMin(value = "1.0", message = "VOUCHER_DISCOUNT_AMOUNT_INVALID")
    BigDecimal discountAmount;

    @DecimalMin(value = "1.0", message = "VOUCHER_DISCOUNT_PERCENT_INVALID")
    @DecimalMax(value = "100.0", message = "VOUCHER_DISCOUNT_PERCENT_INVALID")
    BigDecimal discountPercent;

    LocalDateTime validFrom;

    LocalDateTime validTo;

    String description;

    @Min(value = 1, message = "VOUCHER_USAGE_LIMIT_INVALID")
    Integer usageLimit;

    String category;
}
