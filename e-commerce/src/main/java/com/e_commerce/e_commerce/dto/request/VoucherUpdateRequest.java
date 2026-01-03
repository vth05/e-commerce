package com.e_commerce.e_commerce.dto.request;

import com.e_commerce.e_commerce.validator.ExactlyOneOf;
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
@ExactlyOneOf({"discountAmount", "discountPercent"})
public class VoucherUpdateRequest {
    String code;

    @Min(value = 1, message = "VOUCHER_DISCOUNT_AMOUNT_INVALID")
    Integer discountAmount;

    @Min(value = 1, message = "VOUCHER_DISCOUNT_PERCENT_INVALID")
    @Max(value = 100, message = "VOUCHER_DISCOUNT_PERCENT_INVALID")
    Integer discountPercent;

    @DecimalMin(value = "0", message = "MIN_ORDER_VALUE_INVALID")
    BigDecimal minOrderValue;

    @DecimalMin(value = "0", message = "MAX_DISCOUNT_AMOUNT_INVALID")
    BigDecimal maxDiscountAmount;

    @FutureOrPresent(message = "VOUCHER_VALID_FROM_INVALID")
    LocalDateTime validFrom;

    @Future(message = "VOUCHER_VALID_TO_INVALID")
    LocalDateTime validTo;

    String description;

    @Min(value = 1, message = "VOUCHER_USAGE_LIMIT_INVALID")
    Integer usageLimit;

    String category;

    @Min(value = 0, message = "VOUCHER_USAGE_COUNT_INVALID")
    Integer usageCount;

    Boolean active;
}
