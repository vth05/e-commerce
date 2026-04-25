package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.entity.InventoryReceipt;
import com.e_commerce.e_commerce.enums.InventoryReceiptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryReceiptRepository extends JpaRepository<InventoryReceipt, String> {
    Optional<InventoryReceipt> findByIdAndStatus(String id, InventoryReceiptStatus status);
}
