package com.campusadda.vendorops.forecast.service;

import com.campusadda.vendorops.forecast.dto.request.BulkRunForecastRequest;
import com.campusadda.vendorops.forecast.dto.request.RunForecastRequest;
import com.campusadda.vendorops.forecast.dto.response.ForecastRunResponse;

import java.util.List;

public interface ForecastService {
    ForecastRunResponse runForecast(Long vendorId, RunForecastRequest request);
    List<ForecastRunResponse> runBulkForecast(Long vendorId, BulkRunForecastRequest request);
}