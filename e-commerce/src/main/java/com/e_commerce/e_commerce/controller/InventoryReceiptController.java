package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.request.InventoryReceiptCreationRequest;
import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.dto.response.InventoryReceiptItemResponse;
import com.e_commerce.e_commerce.dto.response.InventoryReceiptResponse;
import com.e_commerce.e_commerce.service.InventoryReceiptService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory-receipts")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class InventoryReceiptController {
    InventoryReceiptService inventoryReceiptService;

    @PostMapping
    ApiResponse<InventoryReceiptResponse> createReceipt(@RequestBody @Valid InventoryReceiptCreationRequest request) {
        return ApiResponse.<InventoryReceiptResponse>builder()
                .result(inventoryReceiptService.createReceipt(request))
                .build();
    }

    @GetMapping("/{receiptId}/items")
    ApiResponse<Page<InventoryReceiptItemResponse>>
    getInventoryReceiptItemsByInventoryReceiptId(
            @PathVariable String receiptId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "productVariantId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ApiResponse.<Page<InventoryReceiptItemResponse>>builder()
                .result(inventoryReceiptService.getInventoryReceiptItemsByInventoryReceiptId(receiptId, page, size, sortBy, sortDir))
                .build();
    }

    @PutMapping("/{receiptId}/complete")
    ApiResponse<InventoryReceiptResponse> completeReceipt(@PathVariable String receiptId) {
        return ApiResponse.<InventoryReceiptResponse>builder()
                .result(inventoryReceiptService.completeReceipt(receiptId))
                .build();
    }

    @PutMapping("/{receiptId}/cancel")
    ApiResponse<InventoryReceiptResponse> cancelReceipt(@PathVariable String receiptId) {
        return ApiResponse.<InventoryReceiptResponse>builder()
                .result(inventoryReceiptService.cancelReceipt(receiptId))
                .build();
    }
}
