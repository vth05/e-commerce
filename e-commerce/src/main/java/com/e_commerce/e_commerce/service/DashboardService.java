package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.response.*;
import com.e_commerce.e_commerce.enums.CheckoutStatus;
import com.e_commerce.e_commerce.repository.OrderItemRepository;
import com.e_commerce.e_commerce.repository.OrderRepository;
import com.e_commerce.e_commerce.util.DateRange;
import com.e_commerce.e_commerce.util.DateRangeUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DashboardService {
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public RevenueStatsResponse getDailyRevenue(CheckoutStatus status, LocalDate start, LocalDate end) {
        if (status == null) status = CheckoutStatus.PAID;
        DateRange dateRange = DateRangeUtils.normalizeDateRange(start, end);
        LocalDateTime startDateTime = dateRange.start();
        LocalDateTime endDateTime = dateRange.end();
        BigDecimal totalRevenue = orderRepository.findTotalRevenueByDateRangeAndCheckoutStatus(status, startDateTime, endDateTime);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }
        List<RevenueByDate> revenueByDates = orderRepository.findDailyRevenueByDateRangeAndCheckoutStatus(status, startDateTime, endDateTime);
        return RevenueStatsResponse.builder()
                .totalRevenue(totalRevenue)
                .revenueByDate(revenueByDates)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductRevenueResponse> getRevenueByProductAndDateRange(LocalDate start, LocalDate end) {
        DateRange dateRange = DateRangeUtils.normalizeDateRange(start, end);
        LocalDateTime startDateTime = dateRange.start();
        LocalDateTime endDateTime = dateRange.end();
        return orderItemRepository.findRevenueByProductAndDateRange(startDateTime, endDateTime);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<TopProductsByRevenueResponse> getTopProductsByRevenueAndDateRange(LocalDate start, LocalDate end, int limit) {
        if (limit > 50) limit = 50;
        DateRange dateRange = DateRangeUtils.normalizeDateRange(start, end);
        LocalDateTime startDateTime = dateRange.start();
        LocalDateTime endDateTime = dateRange.end();
        Pageable pageable = PageRequest.of(0, limit);
        return orderItemRepository.findTopProductsByRevenueAndDateRange(startDateTime, endDateTime, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<TopProductsByQuantitySoldResponse> getTopProductsByQuantitySoldAndDateRange(LocalDate start, LocalDate end, int limit) {
        if (limit > 50) limit = 50;
        DateRange dateRange = DateRangeUtils.normalizeDateRange(start, end);
        LocalDateTime startDateTime = dateRange.start();
        LocalDateTime endDateTime = dateRange.end();
        Pageable pageable = PageRequest.of(0, limit);
        return orderItemRepository.findTopProductsByQuantitySoldAndDateRange(startDateTime, endDateTime, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<TopCustomersResponse> getTopCustomersByDateRangeAndUserStatus(LocalDate start, LocalDate end, boolean active, int limit) {
        if (limit > 50) limit = 50;
        DateRange dateRange = DateRangeUtils.normalizeDateRange(start, end);
        LocalDateTime startDateTime = dateRange.start();
        LocalDateTime endDateTime = dateRange.end();
        List<Object[]> rows = orderRepository.findTopCustomersByDateRangeAndUserStatus(startDateTime, endDateTime, active, limit);
        List<TopCustomersResponse> topCustomersResponses = rows.stream().map(row -> new TopCustomersResponse(
                String.valueOf(row[0]),
                String.valueOf(row[1]),
                String.valueOf(row[2]),
                String.valueOf(row[3]),
                (BigDecimal) row[4],
                (Long) row[5]
        )).toList();
        return topCustomersResponses;
    }
}
