package com.campusadda.vendorops.analytics.service.impl;

import com.campusadda.vendorops.analytics.dto.response.DashboardMetricsResponse;
import com.campusadda.vendorops.analytics.dto.response.RevenueSummaryResponse;
import com.campusadda.vendorops.analytics.dto.response.TopItemAnalyticsResponse;
import com.campusadda.vendorops.analytics.dto.response.OrderStatusSummaryResponse;
import com.campusadda.vendorops.analytics.service.AnalyticsQueryService;
import com.campusadda.vendorops.analytics.service.DashboardMetricsService;
import com.campusadda.vendorops.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardMetricsServiceImpl implements DashboardMetricsService {

    private final AnalyticsQueryService analyticsQueryService;
    private final InventoryService inventoryService;

    @Override
    public DashboardMetricsResponse getDashboardMetrics(Long vendorId, LocalDate fromDate, LocalDate toDate) {
        RevenueSummaryResponse revenueSummary = analyticsQueryService.getRevenueSummary(vendorId, fromDate, toDate);
        OrderStatusSummaryResponse statusSummary = analyticsQueryService.getOrderStatusSummary(vendorId, fromDate, toDate);
        List<TopItemAnalyticsResponse> topItems = analyticsQueryService.getTopItems(vendorId, fromDate, toDate);
        long lowStockCount = inventoryService.getLowStockItems(vendorId).size();

        return DashboardMetricsResponse.builder()
                .vendorId(vendorId)
                .totalOrders(statusSummary.getTotalOrders())
                .completedOrders(statusSummary.getCompletedOrders())
                .cancelledOrders(statusSummary.getCancelledOrders())
                .grossRevenue(revenueSummary.getGrossRevenue())
                .netRevenue(revenueSummary.getNetRevenue())
                .avgOrderValue(revenueSummary.getAvgOrderValue())
                .topItemName(topItems.isEmpty() ? null : topItems.get(0).getItemName())
                .lowStockCount(lowStockCount)
                .build();
    }
}