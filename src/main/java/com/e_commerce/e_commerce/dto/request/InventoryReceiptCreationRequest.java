package com.e_commerce.e_commerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryReceiptCreationRequest {
    String note;

    @NotBlank(message = "SUPPLIER_ID_REQUIRED")
    String supplierId;

    @NotEmpty(message = "INVENTORY_RECEIPT_ITEMS_REQUIRED")
    List<InventoryReceiptItemCreationRequest> inventoryReceiptItemCreationRequests;
}
