package com.e_commerce.e_commerce.mapper;

import com.e_commerce.e_commerce.dto.request.ProductReviewCreationRequest;
import com.e_commerce.e_commerce.dto.request.ProductReviewUpdateRequest;
import com.e_commerce.e_commerce.dto.response.ProductReviewResponse;
import com.e_commerce.e_commerce.entity.ProductReview;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ProductReviewMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ProductReview toProductReview(ProductReviewCreationRequest request);

    ProductReviewResponse toProductReviewResponse(ProductReview productReview);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProductReview(@MappingTarget ProductReview productReview, ProductReviewUpdateRequest request);
}
