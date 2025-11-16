package com.e_commerce.e_commerce.util;

import com.e_commerce.e_commerce.entity.CartItem;

import java.math.BigDecimal;
import java.util.List;

public class CartUtils {
    public static BigDecimal calculateTotalPrice(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(cartItem -> cartItem.getPriceAtPurchase().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
