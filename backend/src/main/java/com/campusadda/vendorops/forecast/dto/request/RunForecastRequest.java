package com.campusadda.vendorops.forecast.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RunForecastRequest {
    private Long menuItemId;
    private String modelName;
    private Integer horizonDays;
    private LocalDate trainingStartDate;
    private LocalDate trainingEndDate;
    private String featureSetVersion;
}