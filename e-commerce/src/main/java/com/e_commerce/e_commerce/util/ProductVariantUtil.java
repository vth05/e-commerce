package com.e_commerce.e_commerce.util;

import java.text.Normalizer;

public class ProductVariantUtil {
    private static String normalize(String input) {
        if (input == null) return "";
        // bỏ dấu
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        // bỏ ký tự đặc biệt
        return normalized.replaceAll("[^a-zA-Z0-9]", "")
                .toUpperCase();
    }

    public static String generateProductVariantName(String productName, String color, String ram, String storage) {
        StringBuilder sb = new StringBuilder(productName);
        if (color != null && !color.isBlank()) {
            sb.append(", ").append(color.trim());
        }
        if (ram != null && !ram.isBlank()) {
            sb.append(", ").append(ram.trim());
        }
        if (storage != null && !storage.isBlank()) {
            sb.append(", ").append(storage.trim());
        }
        return sb.toString();
    }

    public static String generateSku(String productName, String color, String ram, String storage) {
        String baseName = normalize(productName);

        StringBuilder sb = new StringBuilder(baseName);
        if (color != null && !color.isBlank()) {
            sb.append("-").append(normalize(color));
        }
        if (ram != null && !ram.isBlank()) {
            sb.append("-").append(normalize(ram));
        }
        if (storage != null && !storage.isBlank()) {
            sb.append("-").append(normalize(storage));
        }
        return sb.toString();
    }
}
