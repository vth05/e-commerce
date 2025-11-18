package com.e_commerce.e_commerce.entity;

import com.e_commerce.e_commerce.enums.CheckoutStatus;
import com.e_commerce.e_commerce.enums.PaymentMethod;
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
@Table(name = "orders")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Order {
    @Id
    String id;

    String userId;

    @Enumerated(EnumType.STRING)
    CheckoutStatus checkoutStatus;

    String receiverName;

    String receiverPhone;

    String shippingAddress;

    @Enumerated(EnumType.STRING)
    PaymentMethod paymentMethod;

    BigDecimal subtotal;

    BigDecimal shippingFee;

    BigDecimal discount;

    BigDecimal totalPrice;

    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "cart_id")
    Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    Voucher voucher;

    @OneToMany(mappedBy = "order")
    @Builder.Default
    List<OrderItem> orderItems = new ArrayList<>();
}
