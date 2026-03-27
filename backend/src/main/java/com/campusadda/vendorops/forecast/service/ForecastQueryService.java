package com.campusadda.vendorops.forecast.service;

import com.campusadda.vendorops.forecast.dto.response.ForecastRunResponse;
import com.campusadda.vendorops.forecast.dto.response.ForecastValueResponse;
import com.campusadda.vendorops.forecast.dto.response.LatestForecastResponse;

import java.util.List;

public interface ForecastQueryService {
    List<ForecastRunResponse> getForecastRuns(Long vendorId);
    List<ForecastValueResponse> getForecastValues(Long forecastRunId);
    LatestForecastResponse getLatestForecast(Long vendorId, Long menuItemId);
}