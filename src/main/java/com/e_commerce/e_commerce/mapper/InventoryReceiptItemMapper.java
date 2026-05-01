package com.e_commerce.e_commerce.mapper;

import com.e_commerce.e_commerce.dto.response.InventoryReceiptItemResponse;
import com.e_commerce.e_commerce.entity.InventoryReceiptItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryReceiptItemMapper {
    @Mapping(source = "inventoryReceipt.id", target = "inventoryReceiptId")
    InventoryReceiptItemResponse toInventoryReceiptItemResponse(InventoryReceiptItem inventoryReceiptItem);
}
