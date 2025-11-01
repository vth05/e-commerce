package com.e_commerce.e_commerce.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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
public class ProductUpdateRequest {
    @Size(min = 1, message = "PRODUCT_NAME_INVALID")
    String name;

    @Size(min = 1, message = "PRODUCT_CATEGORY_INVALID")
    String category;

    String description;

    Boolean active;

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
