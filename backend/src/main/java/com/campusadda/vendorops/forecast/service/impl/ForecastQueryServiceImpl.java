package com.campusadda.vendorops.forecast.service.impl;

import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.forecast.dto.response.*;
import com.campusadda.vendorops.forecast.entity.ForecastRun;
import com.campusadda.vendorops.forecast.repository.ForecastRunRepository;
import com.campusadda.vendorops.forecast.repository.ForecastValueRepository;
import com.campusadda.vendorops.forecast.service.ForecastQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ForecastQueryServiceImpl implements ForecastQueryService {

    private final ForecastRunRepository forecastRunRepository;
    private final ForecastValueRepository forecastValueRepository;

    @Override
    public List<ForecastRunResponse> getForecastRuns(Long vendorId) {
        return forecastRunRepository.findByVendor_IdOrderByStartedAtDesc(vendorId)
                .stream()
                .map(this::mapRun)
                .toList();
    }

    @Override
    public List<ForecastValueResponse> getForecastValues(Long forecastRunId) {
        return forecastValueRepository.findByForecastRun_IdOrderByForecastDateAsc(forecastRunId)
                .stream()
                .map(value -> ForecastValueResponse.builder()
                        .id(value.getId())
                        .forecastRunId(value.getForecastRun().getId())
                        .forecastDate(value.getForecastDate())
                        .predictedQuantity(value.getPredictedQuantity())
                        .lowerBoundQty(value.getLowerBoundQty())
                        .upperBoundQty(value.getUpperBoundQty())
                        .confidenceLevelPct(value.getConfidenceLevelPct())
                        .build())
                .toList();
    }

    @Override
    public LatestForecastResponse getLatestForecast(Long vendorId, Long menuItemId) {
        ForecastRun run = forecastRunRepository.findByVendor_IdAndMenuItem_IdOrderByStartedAtDesc(vendorId, menuItemId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Forecast not found"));

        return LatestForecastResponse.builder()
                .forecastRun(mapRun(run))
                .values(getForecastValues(run.getId()))
                .build();
    }

    private ForecastRunResponse mapRun(ForecastRun run) {
        return ForecastRunResponse.builder()
                .id(run.getId())
                .vendorId(run.getVendor().getId())
                .menuItemId(run.getMenuItem().getId())
                .modelName(run.getModelName())
                .modelVersion(run.getModelVersion())
                .horizonDays(run.getHorizonDays())
                .trainingStartDate(run.getTrainingStartDate())
                .trainingEndDate(run.getTrainingEndDate())
                .featureSetVersion(run.getFeatureSetVersion())
                .mlflowRunId(run.getMlflowRunId())
                .metricsJson(run.getMetricsJson())
                .status(run.getStatus())
                .startedAt(run.getStartedAt())
                .completedAt(run.getCompletedAt())
                .build();
    }
}