package com.e_commerce.e_commerce.dto.response;

import com.e_commerce.e_commerce.dto.request.GhnOrderItem;
import com.e_commerce.e_commerce.entity.Order;
import lombok.Builder;

import java.util.List;

@Builder
public record CheckoutInternalResponse(
        Order order,
        ShippingFeeCalculationResult shippingFeeCalculationResult,
        List<GhnOrderItem> items
) {
}
