package com.campusadda.vendorops.forecast.client.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class MlForecastRequest {
    private Long vendorId;
    private Long menuItemId;
    private String modelName;
    private Integer horizonDays;
    private String trainingStartDate;
    private String trainingEndDate;
    private List<Map<String, Object>> salesSeries;
    private List<Map<String, Object>> calendarEvents;
}