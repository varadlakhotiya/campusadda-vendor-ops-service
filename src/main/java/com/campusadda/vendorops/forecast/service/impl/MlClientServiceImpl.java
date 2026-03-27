package com.campusadda.vendorops.forecast.service.impl;

import com.campusadda.vendorops.forecast.client.request.MlForecastRequest;
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

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public MlForecastResponse runForecast(MlForecastRequest request) {
        return restTemplate.postForObject(
                mlServiceBaseUrl + "/forecast/run",
                request,
                MlForecastResponse.class
        );
    }
}