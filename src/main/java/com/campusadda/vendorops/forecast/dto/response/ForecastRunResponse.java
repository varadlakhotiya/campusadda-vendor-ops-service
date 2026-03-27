package com.campusadda.vendorops.forecast.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ForecastRunResponse {
    private Long id;
    private Long vendorId;
    private Long menuItemId;
    private String modelName;
    private String modelVersion;
    private Integer horizonDays;
    private LocalDate trainingStartDate;
    private LocalDate trainingEndDate;
    private String featureSetVersion;
    private String mlflowRunId;
    private String metricsJson;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}