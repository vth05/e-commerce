package com.e_commerce.e_commerce.dto.response;

import java.math.BigDecimal;

public record TopCustomersResponse(
        String userId,
        String fullName,
        String email,
        String phoneNumber,
        BigDecimal totalSpent,
        Long totalOrders
) {
}
