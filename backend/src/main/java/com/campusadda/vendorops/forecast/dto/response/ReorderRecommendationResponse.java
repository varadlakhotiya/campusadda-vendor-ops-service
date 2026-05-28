package com.campusadda.vendorops.forecast.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class ReorderRecommendationResponse {
    private Long id;
    private Long vendorId;
    private Long inventoryItemId;
    private Long forecastRunId;
    private LocalDate recommendationDate;
    private BigDecimal currentStockQty;
    private Integer leadTimeDays;
    private BigDecimal forecastDemandQty;
    private BigDecimal safetyStockQty;
    private BigDecimal reorderPointQty;
    private BigDecimal suggestedReorderQty;
    private String recommendationStatus;
    private String explanation;
}