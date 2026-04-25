package com.e_commerce.e_commerce.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReviewUpdateRequest {
    @Min(value = 1, message = "RATING_MIN_INVALID")
    @Max(value = 5, message = "RATING_MAX_INVALID")
    Integer rating;
}
