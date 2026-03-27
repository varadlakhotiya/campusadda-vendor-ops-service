package com.campusadda.vendorops.analytics.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardMetricsResponse {
    private Long vendorId;
    private Integer totalOrders;
    private Integer completedOrders;
    private Integer cancelledOrders;
    private BigDecimal grossRevenue;
    private BigDecimal netRevenue;
    private BigDecimal avgOrderValue;
    private String topItemName;
    private Long lowStockCount;
}