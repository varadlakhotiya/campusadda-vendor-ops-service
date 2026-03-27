package com.campusadda.vendorops.forecast.service;

import com.campusadda.vendorops.forecast.client.request.MlForecastRequest;
import com.campusadda.vendorops.forecast.client.response.MlForecastResponse;

public interface MlClientService {
    MlForecastResponse runForecast(MlForecastRequest request);
}