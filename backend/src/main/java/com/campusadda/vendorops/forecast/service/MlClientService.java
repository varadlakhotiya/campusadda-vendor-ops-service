package com.campusadda.vendorops.forecast.service;

import com.campusadda.vendorops.forecast.client.request.MlForecastRequest;
import com.campusadda.vendorops.forecast.client.request.MlGenerateAnomaliesRequest;
import com.campusadda.vendorops.forecast.client.request.MlGenerateReorderRecommendationsRequest;
import com.campusadda.vendorops.forecast.client.request.MlPredictAndPersistRequest;
import com.campusadda.vendorops.forecast.client.request.MlTrainAndSelectRequest;
import com.campusadda.vendorops.forecast.client.response.MlForecastResponse;

public interface MlClientService {

    MlForecastResponse runForecast(MlForecastRequest request);

    void trainAndSelect(MlTrainAndSelectRequest request);

    void predictAndPersist(MlPredictAndPersistRequest request);

    void generateReorderRecommendations(MlGenerateReorderRecommendationsRequest request);

    void generateAnomalies(MlGenerateAnomaliesRequest request);
}
