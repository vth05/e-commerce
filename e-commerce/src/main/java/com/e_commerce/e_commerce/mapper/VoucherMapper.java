package com.e_commerce.e_commerce.mapper;

import com.e_commerce.e_commerce.dto.request.VoucherCreationRequest;
import com.e_commerce.e_commerce.dto.request.VoucherUpdateRequest;
import com.e_commerce.e_commerce.dto.response.VoucherResponse;
import com.e_commerce.e_commerce.entity.Voucher;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VoucherMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", ignore = true)
    Voucher toVoucher(VoucherCreationRequest request);

    VoucherResponse toVoucherResponse(Voucher voucher);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", ignore = true)
    void updateVoucher(@MappingTarget Voucher voucher, VoucherUpdateRequest request);
}
