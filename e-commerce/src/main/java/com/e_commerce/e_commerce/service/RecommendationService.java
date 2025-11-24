package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.response.TopSellingProductsResponse;
import com.e_commerce.e_commerce.repository.OrderItemRepository;
import com.e_commerce.e_commerce.util.DateRange;
import com.e_commerce.e_commerce.util.DateRangeUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RecommendationService {
    OrderItemRepository orderItemRepository;

    public List<TopSellingProductsResponse> getTopSellingProductsForRecommendationByQuantitySoldAndDateRange(LocalDate start, LocalDate end, int limit) {
        if (limit > 50) limit = 50;
        DateRange dateRange = DateRangeUtils.normalizeDateRange(start, end);
        LocalDateTime startDateTime = dateRange.start();
        LocalDateTime endDateTime = dateRange.end();
        List<Object[]> rows = orderItemRepository.findTopSellingProductsForRecommendationByQuantitySoldAndDateRange(startDateTime, endDateTime, limit);
        List<TopSellingProductsResponse> responses = rows.stream().map(row -> new TopSellingProductsResponse(
                String.valueOf(row[0]),
                String.valueOf(row[1]),
                (BigDecimal) row[2],
                ((Number) row[3]).longValue(),
                ((Number) row[4]).longValue()
        )).toList();
        return responses;
    }
}
