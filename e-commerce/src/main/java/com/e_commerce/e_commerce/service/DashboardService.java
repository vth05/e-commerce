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
        status = resolveStatus(status);
        LocalDateTime[] localDateTimes = resolveDateRange(start, end);
        BigDecimal totalRevenue = orderRepository.findTotalRevenueByDateRangeAndCheckoutStatus(status, localDateTimes[0], localDateTimes[1]);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }
        List<RevenueByDate> revenueByDates = orderRepository.findDailyRevenueByDateRangeAndCheckoutStatus(status, localDateTimes[0], localDateTimes[1]);
        return RevenueStatsResponse.builder()
                .totalRevenue(totalRevenue)
                .revenueByDate(revenueByDates)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<ProductRevenueResponse> getRevenueByProductAndDateRangeAndCheckoutStatus(CheckoutStatus status, LocalDate start, LocalDate end, int page, int limit) {
        status = resolveStatus(status);
        LocalDateTime[] localDateTimes = resolveDateRange(start, end);
        Pageable pageable = PageRequest.of(page, limit);
        return orderItemRepository.findRevenueByProductAndDateRangeAndCheckoutStatus(status, localDateTimes[0], localDateTimes[1], pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<TopProductsByRevenueResponse> getTopProductsByRevenueAndDateRangeAndCheckoutStatus(CheckoutStatus status, LocalDate start, LocalDate end, int page, int limit) {
        if (limit > 50) limit = 50;
        status = resolveStatus(status);
        LocalDateTime[] localDateTimes = resolveDateRange(start, end);
        Pageable pageable = PageRequest.of(page, limit);
        return orderItemRepository.findTopProductsByRevenueAndDateRangeAndCheckoutStatus(status, localDateTimes[0], localDateTimes[1], pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<TopProductsByQuantitySoldResponse> getTopProductsByQuantitySoldAndDateRangeAndCheckoutStatus(CheckoutStatus status, LocalDate start, LocalDate end, int page, int limit) {
        if (limit > 50) limit = 50;
        status = resolveStatus(status);
        LocalDateTime[] localDateTimes = resolveDateRange(start, end);
        Pageable pageable = PageRequest.of(page, limit);
        return orderItemRepository.findTopProductsByQuantitySoldAndDateRangeAndCheckoutStatus(status, localDateTimes[0], localDateTimes[1], pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<TopCustomersResponse> getTopCustomersByUserStatusAndDateRangeAndCheckoutStatus(CheckoutStatus status, LocalDate start, LocalDate end, boolean active, int page, int limit) {
        if (limit > 50) limit = 50;
        status = resolveStatus(status);
        String statusString = status.name();
        LocalDateTime[] localDateTimes = resolveDateRange(start, end);
        List<Object[]> rows = orderRepository.findTopCustomersByUserStatusAndDateRangeAndCheckoutStatus(statusString, localDateTimes[0], localDateTimes[1], active, page * limit, limit);
        return rows.stream().map(row -> new TopCustomersResponse(
                String.valueOf(row[0]),
                String.valueOf(row[1]),
                String.valueOf(row[2]),
                String.valueOf(row[3]),
                (BigDecimal) row[4],
//                (Long) row[5]
                ((Number) row[5]).longValue()
        )).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<ProductVariantLowStockResponse> getProductVariantsLowInStock(int threshold, int page, int limit) {
        if (limit > 50) limit = 50;
        Pageable pageable = PageRequest.of(page, limit);
        return productVariantRepository.findProductVariantsLowInStock(threshold, pageable);
    }

    private CheckoutStatus resolveStatus(CheckoutStatus status) {
        return status != null ? status : CheckoutStatus.PAID;
    }

    private LocalDateTime[] resolveDateRange(LocalDate start, LocalDate end) {
        DateRange dateRange = DateRangeUtils.normalizeDateRange(start, end);
        return new LocalDateTime[]{dateRange.start(), dateRange.end()};
    }
}
