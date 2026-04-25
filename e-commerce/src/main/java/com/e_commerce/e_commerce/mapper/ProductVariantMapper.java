package com.e_commerce.e_commerce.mapper;

import com.e_commerce.e_commerce.dto.request.ProductVariantCreationRequest;
import com.e_commerce.e_commerce.dto.request.ProductVariantUpdateRequest;
import com.e_commerce.e_commerce.dto.response.ProductVariantResponse;
import com.e_commerce.e_commerce.entity.ProductVariant;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ProductVariant toProductVariant(ProductVariantCreationRequest productVariantCreationRequest);

    @Mapping(source = "product.id", target = "productId")
    ProductVariantResponse toProductVariantResponse(ProductVariant productVariant);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProductVariant(@MappingTarget ProductVariant productVariant, ProductVariantUpdateRequest request);
}
