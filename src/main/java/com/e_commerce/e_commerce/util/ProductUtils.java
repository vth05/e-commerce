package com.e_commerce.e_commerce.util;

import java.util.UUID;

public class ProductUtils {
    public static String generateProductCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }
}
