package com.campusadda.vendorops.analytics.controller;

import com.campusadda.vendorops.analytics.dto.response.*;
import com.campusadda.vendorops.analytics.service.AnalyticsQueryService;
import com.campusadda.vendorops.analytics.service.DashboardMetricsService;
import com.campusadda.vendorops.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;
    private final DashboardMetricsService dashboardMetricsService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse>> getDashboard(
            @PathVariable Long vendorId,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.success("Dashboard metrics fetched successfully",
                dashboardMetricsService.getDashboardMetrics(vendorId, fromDate, toDate)));
    }

    @GetMapping("/daily-sales")
    public ResponseEntity<ApiResponse<List<DailySalesTrendResponse>>> getDailySales(
            @PathVariable Long vendorId,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.success("Daily sales fetched successfully",
                analyticsQueryService.getDailySales(vendorId, fromDate, toDate)));
    }

    @GetMapping("/hourly-sales")
    public ResponseEntity<ApiResponse<List<HourlySalesTrendResponse>>> getHourlySales(
            @PathVariable Long vendorId,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.success("Hourly sales fetched successfully",
                analyticsQueryService.getHourlySales(vendorId, fromDate, toDate)));
    }

    @GetMapping("/top-items")
    public ResponseEntity<ApiResponse<List<TopItemAnalyticsResponse>>> getTopItems(
            @PathVariable Long vendorId,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.success("Top items fetched successfully",
                analyticsQueryService.getTopItems(vendorId, fromDate, toDate)));
    }

    @GetMapping("/revenue-summary")
    public ResponseEntity<ApiResponse<RevenueSummaryResponse>> getRevenueSummary(
            @PathVariable Long vendorId,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.success("Revenue summary fetched successfully",
                analyticsQueryService.getRevenueSummary(vendorId, fromDate, toDate)));
    }

    @GetMapping("/order-status-summary")
    public ResponseEntity<ApiResponse<OrderStatusSummaryResponse>> getOrderStatusSummary(
            @PathVariable Long vendorId,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.success("Order status summary fetched successfully",
                analyticsQueryService.getOrderStatusSummary(vendorId, fromDate, toDate)));
    }
}