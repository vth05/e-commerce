package com.e_commerce.e_commerce.util;

import com.e_commerce.e_commerce.entity.CartItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class ShippingUtils {
    private static final BigDecimal FEE_PER_KG = BigDecimal.valueOf(10000);
    private static final BigDecimal FEE_PER_KM = BigDecimal.valueOf(10000);
    private static final BigDecimal MIN_FEE = BigDecimal.valueOf(10000);

    public static BigDecimal calculateShippingFee(List<CartItem> cartItems, BigDecimal distanceKm) {
        if (cartItems == null || cartItems.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal safeDistance = distanceKm.max(BigDecimal.ZERO);

        BigDecimal totalWeight = cartItems.stream()
                .filter(cartItem -> cartItem.isActive())
                .map(cartItem -> cartItem.getProductVariant().getWeight().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(0, RoundingMode.CEILING);

        BigDecimal weightFee = totalWeight.multiply(FEE_PER_KG);

        BigDecimal distanceFee = safeDistance.multiply(FEE_PER_KM);

        BigDecimal shippingFee = weightFee.add(distanceFee);

        return shippingFee.max(MIN_FEE);
    }
}
