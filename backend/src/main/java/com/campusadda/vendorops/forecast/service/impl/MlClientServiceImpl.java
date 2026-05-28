package com.campusadda.vendorops.forecast.service.impl;

import com.campusadda.vendorops.forecast.client.request.MlForecastRequest;
import com.campusadda.vendorops.forecast.client.request.MlGenerateAnomaliesRequest;
import com.campusadda.vendorops.forecast.client.request.MlGenerateReorderRecommendationsRequest;
import com.campusadda.vendorops.forecast.client.request.MlPredictAndPersistRequest;
import com.campusadda.vendorops.forecast.client.request.MlTrainAndSelectRequest;
import com.campusadda.vendorops.forecast.client.response.MlForecastResponse;
import com.campusadda.vendorops.forecast.service.MlClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class MlClientServiceImpl implements MlClientService {

    @Value("${app.ml.service-base-url}")
    private String mlServiceBaseUrl;

    private final RestTemplate mlRestTemplate;

    @Override
    public MlForecastResponse runForecast(MlForecastRequest request) {
        return mlRestTemplate.postForObject(
                mlServiceBaseUrl + "/forecast/run",
                request,
                MlForecastResponse.class
        );
    }

    @Override
    public void trainAndSelect(MlTrainAndSelectRequest request) {
        mlRestTemplate.postForObject(
                mlServiceBaseUrl + "/jobs/train-and-select",
                request,
                Object.class
        );
    }

    @Override
    public void predictAndPersist(MlPredictAndPersistRequest request) {
        mlRestTemplate.postForObject(
                mlServiceBaseUrl + "/jobs/predict-and-persist",
                request,
                Object.class
        );
    }

    @Override
    public void generateReorderRecommendations(MlGenerateReorderRecommendationsRequest request) {
        mlRestTemplate.postForObject(
                mlServiceBaseUrl + "/jobs/reorder/generate",
                request,
                Object.class
        );
    }

    @Override
    public void generateAnomalies(MlGenerateAnomaliesRequest request) {
        mlRestTemplate.postForObject(
                mlServiceBaseUrl + "/jobs/anomalies/generate",
                request,
                Object.class
        );
    }
}
