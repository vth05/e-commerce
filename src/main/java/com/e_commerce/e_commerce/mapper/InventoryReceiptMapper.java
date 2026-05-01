package com.e_commerce.e_commerce.mapper;

import com.e_commerce.e_commerce.dto.response.InventoryReceiptResponse;
import com.e_commerce.e_commerce.entity.InventoryReceipt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryReceiptMapper {
    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "supplier.name", target = "supplierName")
    InventoryReceiptResponse toInventoryReceiptResponse(InventoryReceipt inventoryReceipt);
}
