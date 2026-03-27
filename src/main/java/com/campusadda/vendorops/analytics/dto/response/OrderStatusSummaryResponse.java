package com.campusadda.vendorops.analytics.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderStatusSummaryResponse {
    private Integer totalOrders;
    private Integer completedOrders;
    private Integer cancelledOrders;
}