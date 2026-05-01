package com.e_commerce.e_commerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReviewSummaryResponse {
    Float avgRating;
    Long totalReviews;
    Map<Integer, Long> ratingCounts;
}
