package com.e_commerce.e_commerce.mapper;

import com.e_commerce.e_commerce.dto.request.ProductCreationRequest;
import com.e_commerce.e_commerce.dto.request.ProductUpdateRequest;
import com.e_commerce.e_commerce.dto.response.ProductResponse;
import com.e_commerce.e_commerce.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "category", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Product toProduct(ProductCreationRequest productCreationRequest);

    ProductResponse toProductResponse(Product product);

    @Mapping(target = "category", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProduct(@MappingTarget Product product, ProductUpdateRequest productUpdateRequest);
}
