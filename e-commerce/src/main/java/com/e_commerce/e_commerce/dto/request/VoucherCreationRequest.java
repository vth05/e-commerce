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
public class VoucherCreationRequest {
    @NotBlank(message = "VOUCHER_CODE_REQUIRED")
    String code;

    @DecimalMin(value = "1.0", message = "VOUCHER_DISCOUNT_AMOUNT_INVALID")
    BigDecimal discountAmount;

    @DecimalMin(value = "1.0", message = "VOUCHER_DISCOUNT_PERCENT_INVALID")
    @DecimalMax(value = "100.0", message = "VOUCHER_DISCOUNT_PERCENT_INVALID")
    BigDecimal discountPercent;

    @NotNull(message = "VOUCHER_VALID_FROM_REQUIRED")
    LocalDateTime validFrom;

    @NotNull(message = "VOUCHER_VALID_TO_REQUIRED")
    LocalDateTime validTo;

    String description;

    @NotNull(message = "VOUCHER_USAGE_LIMIT_REQUIRED")
    @Min(value = 1, message = "VOUCHER_USAGE_LIMIT_INVALID")
    Integer usageLimit;

    @NotBlank(message = "VOUCHER_CATEGORY_REQUIRED")
    String category;
}
