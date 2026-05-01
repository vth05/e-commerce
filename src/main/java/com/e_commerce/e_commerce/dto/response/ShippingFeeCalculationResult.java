package com.e_commerce.e_commerce.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ShippingFeeCalculationResult(
        BigDecimal originalShippingFee,
        int weightInGrams,
        int lengthInCm,
        int widthInCm,
        int heightInCm
) {
}
