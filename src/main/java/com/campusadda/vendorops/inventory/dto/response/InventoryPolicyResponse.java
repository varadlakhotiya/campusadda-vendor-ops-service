package com.campusadda.vendorops.inventory.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class InventoryPolicyResponse {
    private Long id;
    private Long inventoryItemId;
    private Integer leadTimeDays;
    private Integer reviewPeriodDays;
    private BigDecimal serviceLevelPct;
    private BigDecimal safetyStockQty;
    private BigDecimal reorderPointQty;
    private BigDecimal minReorderQty;
    private BigDecimal maxReorderQty;
    private String preferredModel;
    private Boolean autoRecommendEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}