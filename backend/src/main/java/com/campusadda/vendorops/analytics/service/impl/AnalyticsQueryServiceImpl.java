package com.campusadda.vendorops.analytics.service.impl;

import com.campusadda.vendorops.analytics.dto.response.*;
import com.campusadda.vendorops.analytics.repository.DailyItemSalesRepository;
import com.campusadda.vendorops.analytics.repository.DailyVendorSalesRepository;
import com.campusadda.vendorops.analytics.repository.HourlyVendorSalesRepository;
import com.campusadda.vendorops.analytics.service.AnalyticsQueryService;
import com.campusadda.vendorops.order.repository.OrderRepository;
import com.campusadda.vendorops.security.VendorAccessService; // ✅ ADD
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


import java.time.LocalDateTime;
import java.math.RoundingMode;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsQueryServiceImpl implements AnalyticsQueryService {

    private final DailyItemSalesRepository dailyItemSalesRepository;
    private final DailyVendorSalesRepository dailyVendorSalesRepository;
    private final HourlyVendorSalesRepository hourlyVendorSalesRepository;
    private final VendorAccessService vendorAccessService; // ✅ ADD
    private final OrderRepository orderRepository;

    @Override
    public List<DailySalesTrendResponse> getDailySales(Long vendorId, LocalDate fromDate, LocalDate toDate) {
        vendorAccessService.validateVendorAccess(vendorId); // 🔐 ADD

        return dailyVendorSalesRepository
                .findByVendor_IdAndSalesDateBetweenOrderBySalesDateAsc(vendorId, fromDate, toDate)
                .stream()
                .map(row -> DailySalesTrendResponse.builder()
                        .salesDate(row.getSalesDate())
                        .totalOrders(row.getTotalOrders())
                        .grossRevenue(row.getGrossRevenue())
                        .netRevenue(row.getNetRevenue())
                        .build())
                .toList();
    }

    @Override
    public List<HourlySalesTrendResponse> getHourlySales(Long vendorId, LocalDate fromDate, LocalDate toDate) {
        vendorAccessService.validateVendorAccess(vendorId); // 🔐 ADD

        return hourlyVendorSalesRepository
                .findByVendor_IdAndSalesDateBetweenOrderBySalesDateAscSalesHourAsc(vendorId, fromDate, toDate)
                .stream()
                .map(row -> HourlySalesTrendResponse.builder()
                        .salesDate(row.getSalesDate())
                        .salesHour(row.getSalesHour() != null 
                                ? row.getSalesHour().intValue() 
                                : null)
                        .totalOrders(row.getTotalOrders())
                        .itemsSoldQty(row.getItemsSoldQty())
                        .revenue(row.getRevenue())
                        .build())
                .toList();
    }

    @Override
    public List<TopItemAnalyticsResponse> getTopItems(Long vendorId, LocalDate fromDate, LocalDate toDate) {
        vendorAccessService.validateVendorAccess(vendorId); // 🔐 ADD

        return dailyItemSalesRepository
                .findTop10ByVendor_IdAndSalesDateBetweenOrderByQuantitySoldDesc(vendorId, fromDate, toDate)
                .stream()
                .map(row -> TopItemAnalyticsResponse.builder()
                        .menuItemId(row.getMenuItem().getId())
                        .itemName(row.getMenuItem().getItemName())
                        .quantitySold(row.getQuantitySold())
                        .grossRevenue(row.getGrossRevenue())
                        .build())
                .toList();
    }

    @Override
public RevenueSummaryResponse getRevenueSummary(Long vendorId, LocalDate fromDate, LocalDate toDate) {

    vendorAccessService.validateVendorAccess(vendorId);

    LocalDateTime start = fromDate.atStartOfDay();
    LocalDateTime end = toDate.plusDays(1).atStartOfDay();

    BigDecimal gross = orderRepository.getCompletedRevenue(vendorId, start, end);

    Long completedOrders =
            orderRepository.countCompletedOrders(vendorId, start, end);

    BigDecimal avgOrderValue = completedOrders == 0
            ? BigDecimal.ZERO
            : gross.divide(BigDecimal.valueOf(completedOrders), 2, RoundingMode.HALF_UP);

    return RevenueSummaryResponse.builder()
            .grossRevenue(gross)
            .netRevenue(gross)
            .avgOrderValue(avgOrderValue)
            .build();
}

    @Override
public OrderStatusSummaryResponse getOrderStatusSummary(
        Long vendorId,
        LocalDate fromDate,
        LocalDate toDate
) {

    vendorAccessService.validateVendorAccess(vendorId);

    LocalDateTime start = fromDate.atStartOfDay();
    LocalDateTime end = toDate.plusDays(1).atStartOfDay();

    int total =
            orderRepository.countTotalOrders(vendorId, start, end).intValue();

    int completed =
            orderRepository.countCompletedOrders(vendorId, start, end).intValue();

    int cancelled =
            orderRepository.countCancelledOrders(vendorId, start, end).intValue();

    return OrderStatusSummaryResponse.builder()
            .totalOrders(total)
            .completedOrders(completed)
            .cancelledOrders(cancelled)
            .build();
}
}