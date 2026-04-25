package com.e_commerce.e_commerce.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReviewCreationRequest {
    @NotBlank(message = "PRODUCT_ID_REQUIRED")
    String productId;
    @NotNull(message = "RATING_REQUIRED")
    @Min(value = 1, message = "RATING_MIN_INVALID")
    @Max(value = 5, message = "RATING_MAX_INVALID")
    Integer rating;
    String comment;
}
