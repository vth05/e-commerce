package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.response.ProductRevenueResponse;
import com.e_commerce.e_commerce.dto.response.RevenueByDate;
import com.e_commerce.e_commerce.dto.response.RevenueStatsResponse;
import com.e_commerce.e_commerce.dto.response.TopProductResponse;
import com.e_commerce.e_commerce.enums.CheckoutStatus;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.repository.OrderItemRepository;
import com.e_commerce.e_commerce.repository.OrderRepository;
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
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DashboardService {
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public RevenueStatsResponse getRevenueStats(CheckoutStatus status, LocalDate start, LocalDate end) {
        if (status == null) status = CheckoutStatus.PAID;
        if (start == null) start = LocalDate.now().minusDays(30);
        if (end == null) end = LocalDate.now();
        if (start.isAfter(end)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);
        BigDecimal totalRevenue = orderRepository.findTotalRevenueByDateRangeAndStatus(status, startDateTime, endDateTime);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }
        List<RevenueByDate> revenueByDates = orderRepository.findDailyRevenueByDateRangeAndStatus(status, startDateTime, endDateTime);
        return RevenueStatsResponse.builder()
                .totalRevenue(totalRevenue)
                .revenueByDate(revenueByDates)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductRevenueResponse> getRevenueByProduct(LocalDate start, LocalDate end) {
        if (start == null) start = LocalDate.now().minusDays(30);
        if (end == null) end = LocalDate.now();
        if (start.isAfter(end)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);
        return orderItemRepository.findRevenueByProduct(startDateTime, endDateTime);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<TopProductResponse> getTopProductsByRevenue(LocalDate start, LocalDate end, int limit) {
        if (limit > 50) limit = 50;
        if (start == null) start = LocalDate.now().minusDays(30);
        if (end == null) end = LocalDate.now();
        if (start.isAfter(end)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);
        Pageable pageable = PageRequest.of(0, limit);
        return orderItemRepository.findTopProductsByRevenueAndDateRange(startDateTime, endDateTime, pageable);
    }
}
