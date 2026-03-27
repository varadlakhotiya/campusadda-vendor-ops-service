package com.campusadda.vendorops.analytics.service.impl;

import com.campusadda.vendorops.analytics.dto.response.*;
import com.campusadda.vendorops.analytics.repository.DailyItemSalesRepository;
import com.campusadda.vendorops.analytics.repository.DailyVendorSalesRepository;
import com.campusadda.vendorops.analytics.repository.HourlyVendorSalesRepository;
import com.campusadda.vendorops.analytics.service.AnalyticsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsQueryServiceImpl implements AnalyticsQueryService {

    private final DailyItemSalesRepository dailyItemSalesRepository;
    private final DailyVendorSalesRepository dailyVendorSalesRepository;
    private final HourlyVendorSalesRepository hourlyVendorSalesRepository;

    @Override
    public List<DailySalesTrendResponse> getDailySales(Long vendorId, LocalDate fromDate, LocalDate toDate) {
        return dailyVendorSalesRepository.findByVendor_IdAndSalesDateBetweenOrderBySalesDateAsc(vendorId, fromDate, toDate)
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
        return hourlyVendorSalesRepository.findByVendor_IdAndSalesDateBetweenOrderBySalesDateAscSalesHourAsc(vendorId, fromDate, toDate)
                .stream()
                .map(row -> HourlySalesTrendResponse.builder()
                        .salesDate(row.getSalesDate())
                        .salesHour(row.getSalesHour())
                        .totalOrders(row.getTotalOrders())
                        .itemsSoldQty(row.getItemsSoldQty())
                        .revenue(row.getRevenue())
                        .build())
                .toList();
    }

    @Override
    public List<TopItemAnalyticsResponse> getTopItems(Long vendorId, LocalDate fromDate, LocalDate toDate) {
        return dailyItemSalesRepository.findTop10ByVendor_IdAndSalesDateBetweenOrderByQuantitySoldDesc(vendorId, fromDate, toDate)
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
        List<com.campusadda.vendorops.analytics.entity.DailyVendorSales> rows =
                dailyVendorSalesRepository.findByVendor_IdAndSalesDateBetweenOrderBySalesDateAsc(vendorId, fromDate, toDate);

        BigDecimal gross = rows.stream().map(r -> r.getGrossRevenue() == null ? BigDecimal.ZERO : r.getGrossRevenue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal net = rows.stream().map(r -> r.getNetRevenue() == null ? BigDecimal.ZERO : r.getNetRevenue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalOrders = rows.stream().mapToInt(r -> r.getTotalOrders() == null ? 0 : r.getTotalOrders()).sum();

        BigDecimal avgOrderValue = totalOrders == 0
                ? BigDecimal.ZERO
                : gross.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP);

        return RevenueSummaryResponse.builder()
                .grossRevenue(gross)
                .netRevenue(net)
                .avgOrderValue(avgOrderValue)
                .build();
    }

    @Override
    public OrderStatusSummaryResponse getOrderStatusSummary(Long vendorId, LocalDate fromDate, LocalDate toDate) {
        List<com.campusadda.vendorops.analytics.entity.DailyVendorSales> rows =
                dailyVendorSalesRepository.findByVendor_IdAndSalesDateBetweenOrderBySalesDateAsc(vendorId, fromDate, toDate);

        int total = rows.stream().mapToInt(r -> r.getTotalOrders() == null ? 0 : r.getTotalOrders()).sum();
        int completed = rows.stream().mapToInt(r -> r.getCompletedOrders() == null ? 0 : r.getCompletedOrders()).sum();
        int cancelled = rows.stream().mapToInt(r -> r.getCancelledOrders() == null ? 0 : r.getCancelledOrders()).sum();

        return OrderStatusSummaryResponse.builder()
                .totalOrders(total)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .build();
    }
}