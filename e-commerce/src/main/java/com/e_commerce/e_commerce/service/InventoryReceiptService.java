package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.InventoryReceiptCreationRequest;
import com.e_commerce.e_commerce.dto.request.InventoryReceiptItemCreationRequest;
import com.e_commerce.e_commerce.dto.response.InventoryReceiptItemResponse;
import com.e_commerce.e_commerce.dto.response.InventoryReceiptResponse;
import com.e_commerce.e_commerce.entity.InventoryReceipt;
import com.e_commerce.e_commerce.entity.InventoryReceiptItem;
import com.e_commerce.e_commerce.entity.ProductVariant;
import com.e_commerce.e_commerce.entity.Supplier;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.enums.InventoryReceiptStatus;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.mapper.InventoryReceiptItemMapper;
import com.e_commerce.e_commerce.mapper.InventoryReceiptMapper;
import com.e_commerce.e_commerce.repository.InventoryReceiptItemRepository;
import com.e_commerce.e_commerce.repository.InventoryReceiptRepository;
import com.e_commerce.e_commerce.repository.ProductVariantRepository;
import com.e_commerce.e_commerce.repository.SupplierRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class InventoryReceiptService {
    SupplierRepository supplierRepository;
    InventoryReceiptRepository inventoryReceiptRepository;
    ProductVariantRepository productVariantRepository;
    InventoryReceiptMapper inventoryReceiptMapper;
    InventoryReceiptItemRepository inventoryReceiptItemRepository;
    InventoryReceiptItemMapper inventoryReceiptItemMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public InventoryReceiptResponse createReceipt(InventoryReceiptCreationRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId()).orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_EXISTED));
        InventoryReceipt inventoryReceipt = new InventoryReceipt();
        inventoryReceipt.setStatus(InventoryReceiptStatus.PENDING);
        inventoryReceiptRepository.save(inventoryReceipt);
        List<InventoryReceiptItemCreationRequest> inventoryReceiptItemCreationRequests = request.getInventoryReceiptItemCreationRequests();
        Set<String> productVariantIds = inventoryReceiptItemCreationRequests.stream()
                .map(inventoryReceiptItemCreationRequest -> inventoryReceiptItemCreationRequest.getProductVariantId())
                .collect(Collectors.toSet());
        long existingVariantsCount = productVariantRepository.countByIdIn(productVariantIds);
        if (productVariantIds.size() != existingVariantsCount) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED);
        }
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<InventoryReceiptItem> inventoryReceiptItems = new ArrayList<>();
        for (InventoryReceiptItemCreationRequest inventoryReceiptItemCreationRequest : inventoryReceiptItemCreationRequests) {
            String productVariantId = inventoryReceiptItemCreationRequest.getProductVariantId();
            Long quantity = inventoryReceiptItemCreationRequest.getQuantity();
            BigDecimal importPrice = inventoryReceiptItemCreationRequest.getImportPrice();
            BigDecimal subtotal = BigDecimal.valueOf(quantity).multiply(importPrice);
            inventoryReceiptItems.add(InventoryReceiptItem.builder()
                    .inventoryReceipt(inventoryReceipt)
                    .productVariantId(productVariantId)
                    .quantity(quantity)
                    .importPrice(importPrice)
                    .totalPrice(subtotal)
                    .build());
            totalPrice = totalPrice.add(subtotal);
        }
        inventoryReceipt.setTotalPrice(totalPrice);
        inventoryReceiptItemRepository.saveAll(inventoryReceiptItems);
        if (request.getNote() != null) {
            inventoryReceipt.setNote(request.getNote());
        }
        inventoryReceipt.setSupplier(supplier);
        log.info(inventoryReceipt.getStatus().toString());
        return inventoryReceiptMapper.toInventoryReceiptResponse(inventoryReceipt);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public InventoryReceiptResponse completeReceipt(String receiptId) {
        InventoryReceipt receipt = inventoryReceiptRepository.findByIdAndStatus(receiptId, InventoryReceiptStatus.PENDING).orElseThrow(() -> new AppException(ErrorCode.INVENTORY_RECEIPT_NOT_EXISTED));
        List<InventoryReceiptItem> inventoryReceiptItems = inventoryReceiptItemRepository.findAllByInventoryReceiptId(receiptId);
        Map<String, Long> itemQuantities = new HashMap<>();
        Set<String> productVariantIds = inventoryReceiptItems.stream().map(inventoryReceiptItem -> {
            itemQuantities.put(
                    inventoryReceiptItem.getProductVariantId(),
                    inventoryReceiptItem.getQuantity() + itemQuantities.getOrDefault(inventoryReceiptItem.getProductVariantId(), 0L)
            );
            return inventoryReceiptItem.getProductVariantId();
        }).collect(Collectors.toSet());
        List<ProductVariant> productVariants = productVariantRepository.findAllByIdIn(productVariantIds);
        if (productVariantIds.size() != productVariants.size()) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXISTED);
        }
        productVariants.forEach(productVariant -> productVariant.setQuantity(productVariant.getQuantity() + itemQuantities.get(productVariant.getId())));
        receipt.setStatus(InventoryReceiptStatus.COMPLETED);
        return inventoryReceiptMapper.toInventoryReceiptResponse(inventoryReceiptRepository.save(receipt));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public InventoryReceiptResponse cancelReceipt(String receiptId) {
        InventoryReceipt receipt = inventoryReceiptRepository.findByIdAndStatusNot(receiptId, InventoryReceiptStatus.COMPLETED).orElseThrow(() -> new AppException(ErrorCode.INVENTORY_RECEIPT_NOT_EXISTED));
        receipt.setStatus(InventoryReceiptStatus.CANCELLED);
        return inventoryReceiptMapper.toInventoryReceiptResponse(inventoryReceiptRepository.save(receipt));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<InventoryReceiptItemResponse> getInventoryReceiptItemsByInventoryReceiptId(String inventoryReceiptId, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<InventoryReceiptItem> inventoryReceiptItems = inventoryReceiptItemRepository.findAllByInventoryReceiptId(inventoryReceiptId, pageable);
        return inventoryReceiptItems.map(inventoryReceiptItem -> inventoryReceiptItemMapper.toInventoryReceiptItemResponse(inventoryReceiptItem));
    }
}
