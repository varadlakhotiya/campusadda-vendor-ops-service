package com.campusadda.vendorops.anomaly.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class AnomalyResponse {
    private Long id;
    private Long vendorId;
    private Long menuItemId;
    private String menuItemName;
    private LocalDate anomalyDate;
    private String anomalyType;
    private BigDecimal observedValue;
    private BigDecimal expectedValue;
    private BigDecimal deviationScore;
    private String severity;
    private String status;
    private String detailsJson;
}