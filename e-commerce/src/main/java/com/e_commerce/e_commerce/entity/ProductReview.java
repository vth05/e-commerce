package com.e_commerce.e_commerce.entity;

import com.e_commerce.e_commerce.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ProductReview {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String productId;

    String userId;

    int rating;

    String comment;

    String rejectionReason;

    @Enumerated(EnumType.STRING)
    ReviewStatus reviewStatus;

    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    LocalDateTime updatedAt;
}
