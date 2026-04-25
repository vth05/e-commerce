package com.e_commerce.e_commerce.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSearchRequest {
    String keyword;

    List<String> brands;

    List<String> rams;

    List<String> storages;

    List<String> cpus;

    List<String> gpus;

    List<BigDecimal> screenSizes;

    List<String> screenResolutions;

    List<Integer> refreshRatesHz;

    BigDecimal minPrice;

    BigDecimal maxPrice;
}
