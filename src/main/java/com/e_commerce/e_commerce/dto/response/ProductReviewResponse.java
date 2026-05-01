package com.e_commerce.e_commerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReviewResponse {
    String id;
    String productId;
    String userId;
    Integer rating;
    String comment;
    String rejectionReason;
    String reviewStatus;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
