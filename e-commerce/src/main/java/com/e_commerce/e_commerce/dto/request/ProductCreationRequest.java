package com.e_commerce.e_commerce.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class ProductCreationRequest {
    @NotBlank(message = "PRODUCT_NAME_REQUIRED")
    String name;

    @NotBlank(message = "PRODUCT_CATEGORY_REQUIRED")
    String category;

    String description;

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public void setCategory(String category) {
        this.category = category == null ? null : category.trim();
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}
