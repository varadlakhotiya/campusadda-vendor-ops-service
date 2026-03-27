package com.campusadda.vendorops.forecast.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LatestForecastResponse {
    private ForecastRunResponse forecastRun;
    private List<ForecastValueResponse> values;
}