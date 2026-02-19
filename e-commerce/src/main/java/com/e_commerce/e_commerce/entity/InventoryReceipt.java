package com.e_commerce.e_commerce.entity;

import com.e_commerce.e_commerce.enums.InventoryReceiptStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class InventoryReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    BigDecimal totalPrice;

    String note;

    @Enumerated(EnumType.STRING)
    InventoryReceiptStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    Supplier supplier;

    @OneToMany(mappedBy = "inventoryReceipt")
    @Builder.Default
    List<InventoryReceiptItem> inventoryReceiptItems = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    LocalDateTime updatedAt;
}
