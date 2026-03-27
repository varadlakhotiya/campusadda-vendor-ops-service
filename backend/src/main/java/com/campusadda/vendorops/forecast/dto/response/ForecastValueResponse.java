package com.campusadda.vendorops.forecast.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class ForecastValueResponse {
    private Long id;
    private Long forecastRunId;
    private LocalDate forecastDate;
    private BigDecimal predictedQuantity;
    private BigDecimal lowerBoundQty;
    private BigDecimal upperBoundQty;
    private BigDecimal confidenceLevelPct;
}