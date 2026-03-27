package com.campusadda.vendorops.analytics.service;

import com.campusadda.vendorops.analytics.dto.response.DashboardMetricsResponse;

import java.time.LocalDate;

public interface DashboardMetricsService {
    DashboardMetricsResponse getDashboardMetrics(Long vendorId, LocalDate fromDate, LocalDate toDate);
}