package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.entity.ProductReview;
import com.e_commerce.e_commerce.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, String> {
    Page<ProductReview> findAllByProductIdAndReviewStatus(String productId, ReviewStatus reviewStatus, Pageable pageable);

    Page<ProductReview> findAllByProductId(String productId, Pageable pageable);

    Page<ProductReview> findAllByReviewStatus(ReviewStatus reviewStatus, Pageable pageable);

    @Query("""
            select avg(pr.rating)
            from ProductReview pr
            where pr.productId = :productId and pr.reviewStatus = 'APPROVED'
            """)
    Float findAverageRatingByProductId(@Param("productId") String productId);

    @Query("""
            select pr.rating, count(pr.rating)
            from ProductReview pr
            where pr.productId = :productId and pr.reviewStatus = 'APPROVED'
            group by pr.rating
            """)
    List<Object[]> countRatingsGroupByStars(@Param("productId") String productId);
}
