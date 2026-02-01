package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.response.*;
import com.e_commerce.e_commerce.enums.CheckoutStatus;
import com.e_commerce.e_commerce.repository.OrderItemRepository;
import com.e_commerce.e_commerce.repository.OrderRepository;
import com.e_commerce.e_commerce.repository.ProductVariantRepository;
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
    ProductVariantRepository productVariantRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public RevenueStatsResponse getDailyRevenueByDateRangeAndCheckoutStatus(CheckoutStatus status, LocalDate start, LocalDate end) {
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
    public Page<ProductRevenueResponse> getRevenueByProductAndDateRangeAndCheckoutStatus(CheckoutStatus status, LocalDate start, LocalDate end, int page, int limit) {
        if (status == null) status = CheckoutStatus.PAID;
        DateRange dateRange = DateRangeUtils.normalizeDateRange(start, end);
        LocalDateTime startDateTime = dateRange.start();
        LocalDateTime endDateTime = dateRange.end();
        Pageable pageable = PageRequest.of(page, limit);
        return orderItemRepository.findRevenueByProductAndDateRangeAndCheckoutStatus(status, startDateTime, endDateTime, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<TopProductsByRevenueResponse> getTopProductsByRevenueAndDateRangeAndCheckoutStatus(CheckoutStatus status, LocalDate start, LocalDate end, int page, int limit) {
        if (limit > 50) limit = 50;
        if (status == null) status = CheckoutStatus.PAID;
        DateRange dateRange = DateRangeUtils.normalizeDateRange(start, end);
        LocalDateTime startDateTime = dateRange.start();
        LocalDateTime endDateTime = dateRange.end();
        Pageable pageable = PageRequest.of(page, limit);
        return orderItemRepository.findTopProductsByRevenueAndDateRangeAndCheckoutStatus(status, startDateTime, endDateTime, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<TopProductsByQuantitySoldResponse> getTopProductsByQuantitySoldAndDateRangeAndCheckoutStatus(CheckoutStatus status, LocalDate start, LocalDate end, int page, int limit) {
        if (limit > 50) limit = 50;
        if (status == null) status = CheckoutStatus.PAID;
        DateRange dateRange = DateRangeUtils.normalizeDateRange(start, end);
        LocalDateTime startDateTime = dateRange.start();
        LocalDateTime endDateTime = dateRange.end();
        Pageable pageable = PageRequest.of(page, limit);
        return orderItemRepository.findTopProductsByQuantitySoldAndDateRangeAndCheckoutStatus(status, startDateTime, endDateTime, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<TopCustomersResponse> getTopCustomersByUserStatusAndDateRangeAndCheckoutStatus(CheckoutStatus status, LocalDate start, LocalDate end, boolean active, int page, int limit) {
        if (limit > 50) limit = 50;
        if (status == null) status = CheckoutStatus.PAID;
        String statusString = status.name();
        DateRange dateRange = DateRangeUtils.normalizeDateRange(start, end);
        LocalDateTime startDateTime = dateRange.start();
        LocalDateTime endDateTime = dateRange.end();
        List<Object[]> rows = orderRepository.findTopCustomersByUserStatusAndDateRangeAndCheckoutStatus(statusString, startDateTime, endDateTime, active, page * limit, limit);
        List<TopCustomersResponse> topCustomersResponses = rows.stream().map(row -> new TopCustomersResponse(
                String.valueOf(row[0]),
                String.valueOf(row[1]),
                String.valueOf(row[2]),
                String.valueOf(row[3]),
                (BigDecimal) row[4],
//                (Long) row[5]
                ((Number) row[5]).longValue()
        )).toList();
        return topCustomersResponses;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<ProductVariantLowStockResponse> getProductVariantsLowInStock(int threshold, int page, int limit) {
        if (limit > 50) limit = 50;
        Pageable pageable = PageRequest.of(page, limit);
        return productVariantRepository.findProductVariantsLowInStock(threshold, pageable);
    }
}
