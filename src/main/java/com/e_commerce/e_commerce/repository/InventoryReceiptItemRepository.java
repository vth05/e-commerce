package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.entity.InventoryReceiptItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryReceiptItemRepository extends JpaRepository<InventoryReceiptItem, String> {
    Page<InventoryReceiptItem> findAllByInventoryReceiptId(String inventoryReceiptId, Pageable pageable);

    List<InventoryReceiptItem> findAllByInventoryReceiptId(String inventoryReceiptId);
}
