package com.campusadda.vendorops.forecast.client.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MlForecastResponse {
    private String modelVersion;
    private String mlflowRunId;
    private Map<String, Object> metrics;
    private List<ForecastPoint> forecasts;

    @Getter
    @Setter
    public static class ForecastPoint {
        private String forecastDate;
        private Double predictedQuantity;
        private Double lowerBoundQty;
        private Double upperBoundQty;
        private Double confidenceLevelPct;
    }
}