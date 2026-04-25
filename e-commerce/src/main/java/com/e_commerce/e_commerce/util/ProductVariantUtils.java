package com.e_commerce.e_commerce.util;

import java.math.BigDecimal;
import java.text.Normalizer;

public class ProductVariantUtils {
    private static String normalize(String input) {
        if (input == null) return "";
        // bỏ dấu
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        // bỏ ký tự đặc biệt
        return normalized.replaceAll("[^a-zA-Z0-9]", "")
                .toUpperCase();
    }

    public static String generateProductVariantName(
            String productName,
            String cpu,
            String ram,
            String storage,
            String gpu,
            BigDecimal screenSize,
            String screenResolution,
            Integer refreshRateHz
    ) {
        StringBuilder sb = new StringBuilder(productName);
        if (cpu != null && !cpu.isBlank()) {
            sb.append(", ").append(cpu.trim());
        }
        if (ram != null && !ram.isBlank()) {
            sb.append(", RAM ").append(ram.trim());
        }
        if (storage != null && !storage.isBlank()) {
            sb.append(", SSD ").append(storage.trim());
        }
        if (gpu != null && !gpu.isBlank()) {
            sb.append(", ").append(gpu.trim());
        }
        if (screenSize != null) {
            sb.append(", ").append(screenSize).append("″");
        }
        if (screenResolution != null && !screenResolution.isBlank()) {
            sb.append(", ").append(screenResolution.trim());
        }
        if (refreshRateHz != null) {
            sb.append(", ").append(refreshRateHz).append("Hz");
        }
        return sb.toString();
    }

    public static String generateSku(String productCode, String color, String ram, String storage) {
        StringBuilder sb = new StringBuilder(normalize(productCode));
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
