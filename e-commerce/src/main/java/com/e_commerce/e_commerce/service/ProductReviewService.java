package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.ProductReviewCreationRequest;
import com.e_commerce.e_commerce.dto.request.ProductReviewUpdateRequest;
import com.e_commerce.e_commerce.dto.response.ProductReviewResponse;
import com.e_commerce.e_commerce.dto.response.ProductReviewSummaryResponse;
import com.e_commerce.e_commerce.entity.ProductReview;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.enums.ReviewStatus;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.mapper.ProductReviewMapper;
import com.e_commerce.e_commerce.repository.ProductRepository;
import com.e_commerce.e_commerce.repository.ProductReviewRepository;
import com.e_commerce.e_commerce.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductReviewService {
    ProductReviewRepository productReviewRepository;
    ProductReviewMapper productReviewMapper;
    ProductRepository productRepository;

    @PreAuthorize("hasRole('USER')")
    public ProductReviewResponse createProductReview(ProductReviewCreationRequest request) {
        validateProductExists(request.getProductId());
        ProductReview productReview = productReviewMapper.toProductReview(request);
        productReview.setUserId(SecurityUtils.getUserIdFromAuthentication());
        productReview.setReviewStatus(ReviewStatus.PENDING);
        return productReviewMapper.toProductReviewResponse(productReviewRepository.save(productReview));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ProductReviewResponse updateProductReview(String id, ProductReviewUpdateRequest request) {
        ProductReview productReview = productReviewRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_REVIEW_NOT_EXISTED));
        boolean isAdmin = SecurityUtils.isAdmin();
        if (!isAdmin) {
            checkOwnership(productReview);
            productReview.setReviewStatus(ReviewStatus.PENDING);
        }
        productReviewMapper.updateProductReview(productReview, request);
        return productReviewMapper.toProductReviewResponse(productReviewRepository.save(productReview));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductReviewResponse getProductReview(String id) {
        return productReviewMapper.toProductReviewResponse(productReviewRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_REVIEW_NOT_EXISTED)));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ProductReviewResponse deleteProductReview(String id) {
        ProductReview productReview = productReviewRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_REVIEW_NOT_EXISTED));
        boolean isAdmin = SecurityUtils.isAdmin();
        if (!isAdmin) {
            checkOwnership(productReview);
        }
        productReview.setReviewStatus(ReviewStatus.DELETED);
        return productReviewMapper.toProductReviewResponse(productReviewRepository.save(productReview));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductReviewResponse approveProductReview(String id) {
        ProductReview productReview = productReviewRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_REVIEW_NOT_EXISTED));
        productReview.setReviewStatus(ReviewStatus.APPROVED);
        return productReviewMapper.toProductReviewResponse(productReviewRepository.save(productReview));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductReviewResponse rejectProductReview(String id, String reason) {
        ProductReview productReview = productReviewRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_REVIEW_NOT_EXISTED));
        productReview.setReviewStatus(ReviewStatus.REJECTED);
        productReview.setRejectionReason(reason);
        return productReviewMapper.toProductReviewResponse(productReviewRepository.save(productReview));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<ProductReviewResponse> getProductReviews(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productReviewRepository.findAll(pageable).map(productReview -> productReviewMapper.toProductReviewResponse(productReview));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<ProductReviewResponse> getPendingProductReviewsForAdmin(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productReviewRepository.findAllByReviewStatus(ReviewStatus.PENDING, pageable).map(productReview -> productReviewMapper.toProductReviewResponse(productReview));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Page<ProductReviewResponse> getProductReviewsByProductId(String productId, int page, int size, String sortBy, String sortDir) {
        validateProductExists(productId);
        boolean isAdmin = SecurityUtils.isAdmin();

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductReview> productReviews;
        if (isAdmin) {
            productReviews = productReviewRepository.findAllByProductId(productId, pageable);
        } else {
            productReviews = productReviewRepository.findAllByProductIdAndReviewStatus(productId, ReviewStatus.APPROVED, pageable);
        }

        return productReviews.map(productReview -> productReviewMapper.toProductReviewResponse(productReview));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ProductReviewSummaryResponse getProductReviewMetadataByProductId(String productId) {
        validateProductExists(productId);
        Float avgRating = productReviewRepository.findAverageRatingByProductId(productId);
        List<Object[]> rawRatingCounts = productReviewRepository.countRatingsGroupByStars(productId);
        Map<Integer, Long> ratingCounts = new HashMap<>();
        for (int i = 1; i <= 5; i++) ratingCounts.put(i, 0L);
        Long totalReviews = 0L;
        for (Object[] rawRatingCount : rawRatingCounts) {
            ratingCounts.put(((Number) rawRatingCount[0]).intValue(), ((Number) rawRatingCount[1]).longValue());
            totalReviews += ((Number) rawRatingCount[1]).longValue();
        }
        return ProductReviewSummaryResponse.builder()
                .avgRating(avgRating)
                .totalReviews(totalReviews)
                .ratingCounts(ratingCounts)
                .build();
    }

    private void checkOwnership(ProductReview productReview) {
        String userId = SecurityUtils.getUserIdFromAuthentication();
        if (!userId.equals(productReview.getUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateProductExists(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        }
    }
}
