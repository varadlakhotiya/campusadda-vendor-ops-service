package com.campusadda.vendorops.forecast.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BulkRunForecastRequest {
    private String modelName;
    private Integer horizonDays;
}