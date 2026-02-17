package com.e_commerce.e_commerce.mapper;

import com.e_commerce.e_commerce.dto.request.SupplierCreationRequest;
import com.e_commerce.e_commerce.dto.request.SupplierUpdateRequest;
import com.e_commerce.e_commerce.dto.response.SupplierResponse;
import com.e_commerce.e_commerce.entity.Supplier;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Supplier toSupplier(SupplierCreationRequest request);

    SupplierResponse toSupplierResponse(Supplier supplier);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSupplier(@MappingTarget Supplier supplier, SupplierUpdateRequest request);
}
