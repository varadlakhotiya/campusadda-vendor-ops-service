package com.campusadda.vendorops.inventory.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpsertInventoryPolicyRequest {
    private Integer leadTimeDays;
    private Integer reviewPeriodDays;
    private BigDecimal serviceLevelPct;
    private BigDecimal safetyStockQty;
    private BigDecimal reorderPointQty;
    private BigDecimal minReorderQty;
    private BigDecimal maxReorderQty;
    private String preferredModel;
    private Boolean autoRecommendEnabled;
}