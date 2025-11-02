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

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Checkout {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String userId;

    @Enumerated(EnumType.STRING)
    CheckoutStatus checkoutStatus;

    String shippingAddress;

    @Enumerated(EnumType.STRING)
    PaymentMethod paymentMethod;

    BigDecimal totalPrice;

    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    LocalDateTime updatedAt;

    @OneToOne()
    @JoinColumn(name = "cart_id")
    Cart cart;
}
