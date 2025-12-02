package com.e_commerce.e_commerce.dto.request;

import com.e_commerce.e_commerce.validator.ExactlyOneOf;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@ExactlyOneOf({"discountAmount", "discountPercent"})
public class VoucherCreationRequest {
    @NotBlank(message = "VOUCHER_CODE_REQUIRED")
    String code;

    @Min(value = 1, message = "VOUCHER_DISCOUNT_AMOUNT_INVALID")
    Integer discountAmount;

    @Min(value = 1, message = "VOUCHER_DISCOUNT_PERCENT_INVALID")
    @Max(value = 100, message = "VOUCHER_DISCOUNT_PERCENT_INVALID")
    Integer discountPercent;

    @NotNull(message = "VOUCHER_VALID_FROM_REQUIRED")
    @FutureOrPresent(message = "VOUCHER_VALID_FROM_INVALID")
    LocalDateTime validFrom;

    @NotNull(message = "VOUCHER_VALID_TO_REQUIRED")
    @Future(message = "VOUCHER_VALID_TO_INVALID")
    LocalDateTime validTo;

    String description;

    @NotNull(message = "VOUCHER_USAGE_LIMIT_REQUIRED")
    @Min(value = 1, message = "VOUCHER_USAGE_LIMIT_INVALID")
    Integer usageLimit;

    @NotBlank(message = "VOUCHER_CATEGORY_REQUIRED")
    String category;
}
