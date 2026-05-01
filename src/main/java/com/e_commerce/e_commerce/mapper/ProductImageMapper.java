package com.e_commerce.e_commerce.mapper;

import com.e_commerce.e_commerce.dto.response.ProductImageResponse;
import com.e_commerce.e_commerce.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {
    ProductImageResponse toProductImageResponse(ProductImage productImage);
}
