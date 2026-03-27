package com.campusadda.vendorops.analytics.service;

import com.campusadda.vendorops.analytics.dto.response.*;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsQueryService {
    List<DailySalesTrendResponse> getDailySales(Long vendorId, LocalDate fromDate, LocalDate toDate);
    List<HourlySalesTrendResponse> getHourlySales(Long vendorId, LocalDate fromDate, LocalDate toDate);
    List<TopItemAnalyticsResponse> getTopItems(Long vendorId, LocalDate fromDate, LocalDate toDate);
    RevenueSummaryResponse getRevenueSummary(Long vendorId, LocalDate fromDate, LocalDate toDate);
    OrderStatusSummaryResponse getOrderStatusSummary(Long vendorId, LocalDate fromDate, LocalDate toDate);
}