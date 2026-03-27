package com.campusadda.vendorops.analytics.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryRiskSummaryResponse {
    private Long lowStockCount;
    private Long criticalStockCount;
}