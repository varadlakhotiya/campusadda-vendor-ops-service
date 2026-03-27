package com.campusadda.vendorops.forecast.service.impl;

import com.campusadda.vendorops.analytics.repository.DailyItemSalesRepository;
import com.campusadda.vendorops.forecast.client.request.MlForecastRequest;
import com.campusadda.vendorops.forecast.client.response.MlForecastResponse;
import com.campusadda.vendorops.forecast.dto.request.BulkRunForecastRequest;
import com.campusadda.vendorops.forecast.dto.request.RunForecastRequest;
import com.campusadda.vendorops.forecast.dto.response.ForecastRunResponse;
import com.campusadda.vendorops.forecast.entity.ForecastRun;
import com.campusadda.vendorops.forecast.entity.ForecastValue;
import com.campusadda.vendorops.forecast.repository.CalendarEventRepository;
import com.campusadda.vendorops.forecast.repository.ForecastRunRepository;
import com.campusadda.vendorops.forecast.repository.ForecastValueRepository;
import com.campusadda.vendorops.forecast.service.ForecastService;
import com.campusadda.vendorops.forecast.service.MlClientService;
import com.campusadda.vendorops.menu.entity.MenuItem;
import com.campusadda.vendorops.menu.repository.MenuItemRepository;
import com.campusadda.vendorops.vendor.entity.Vendor;
import com.campusadda.vendorops.vendor.validator.VendorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ForecastServiceImpl implements ForecastService {

    private final VendorValidator vendorValidator;
    private final MenuItemRepository menuItemRepository;
    private final ForecastRunRepository forecastRunRepository;
    private final ForecastValueRepository forecastValueRepository;
    private final DailyItemSalesRepository dailyItemSalesRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final MlClientService mlClientService;

    @Override
    public ForecastRunResponse runForecast(Long vendorId, RunForecastRequest request) {
        Vendor vendor = vendorValidator.validateVendorExists(vendorId);
        MenuItem menuItem = menuItemRepository.findByIdAndVendor_Id(request.getMenuItemId(), vendorId)
                .orElseThrow(() -> new com.campusadda.vendorops.common.exception.ResourceNotFoundException("Menu item not found"));

        ForecastRun run = new ForecastRun();
        run.setVendor(vendor);
        run.setMenuItem(menuItem);
        run.setModelName(request.getModelName() != null ? request.getModelName() : "XGBOOST");
        run.setModelVersion("v1");
        run.setHorizonDays(request.getHorizonDays() != null ? request.getHorizonDays() : 7);
        run.setTrainingStartDate(request.getTrainingStartDate());
        run.setTrainingEndDate(request.getTrainingEndDate());
        run.setFeatureSetVersion(request.getFeatureSetVersion());
        run.setStatus("RUNNING");
        run.setStartedAt(LocalDateTime.now());
        forecastRunRepository.save(run);

        List<Map<String, Object>> salesSeries = dailyItemSalesRepository
                .findByVendor_IdAndSalesDateBetweenOrderBySalesDateAsc(vendorId, request.getTrainingStartDate(), request.getTrainingEndDate())
                .stream()
                .filter(row -> row.getMenuItem().getId().equals(menuItem.getId()))
                .map(row -> Map.<String, Object>of(
                        "date", row.getSalesDate().toString(),
                        "quantitySold", row.getQuantitySold(),
                        "grossRevenue", row.getGrossRevenue()
                ))
                .toList();

        List<Map<String, Object>> calendarEvents = calendarEventRepository
                .findByEventDateBetweenAndIsActiveTrue(request.getTrainingStartDate(), request.getTrainingEndDate())
                .stream()
                .map(event -> Map.<String, Object>of(
                        "eventDate", event.getEventDate().toString(),
                        "eventType", event.getEventType(),
                        "impactLevel", event.getImpactLevel()
                ))
                .toList();

        MlForecastRequest mlRequest = MlForecastRequest.builder()
                .vendorId(vendorId)
                .menuItemId(menuItem.getId())
                .modelName(run.getModelName())
                .horizonDays(run.getHorizonDays())
                .trainingStartDate(request.getTrainingStartDate().toString())
                .trainingEndDate(request.getTrainingEndDate().toString())
                .salesSeries(salesSeries)
                .calendarEvents(calendarEvents)
                .build();

        MlForecastResponse mlResponse = mlClientService.runForecast(mlRequest);

        run.setModelVersion(mlResponse.getModelVersion());
        run.setMlflowRunId(mlResponse.getMlflowRunId());
        run.setMetricsJson(String.valueOf(mlResponse.getMetrics()));
        run.setStatus("SUCCESS");
        run.setCompletedAt(LocalDateTime.now());
        forecastRunRepository.save(run);

        for (MlForecastResponse.ForecastPoint point : mlResponse.getForecasts()) {
            ForecastValue value = new ForecastValue();
            value.setForecastRun(run);
            value.setForecastDate(java.time.LocalDate.parse(point.getForecastDate()));
            value.setPredictedQuantity(BigDecimal.valueOf(point.getPredictedQuantity()));
            value.setLowerBoundQty(point.getLowerBoundQty() != null ? BigDecimal.valueOf(point.getLowerBoundQty()) : null);
            value.setUpperBoundQty(point.getUpperBoundQty() != null ? BigDecimal.valueOf(point.getUpperBoundQty()) : null);
            value.setConfidenceLevelPct(point.getConfidenceLevelPct() != null ? BigDecimal.valueOf(point.getConfidenceLevelPct()) : null);
            forecastValueRepository.save(value);
        }

        return ForecastRunResponse.builder()
                .id(run.getId())
                .vendorId(vendorId)
                .menuItemId(menuItem.getId())
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

    @Override
    public List<ForecastRunResponse> runBulkForecast(Long vendorId, BulkRunForecastRequest request) {
        List<MenuItem> menuItems = menuItemRepository.findByVendor_IdAndIsAvailableTrueAndIsActiveTrueOrderByDisplayOrderAsc(vendorId);

        return menuItems.stream().map(menuItem -> {
            RunForecastRequest single = new RunForecastRequest();
            single.setMenuItemId(menuItem.getId());
            single.setModelName(request.getModelName());
            single.setHorizonDays(request.getHorizonDays());
            single.setTrainingStartDate(java.time.LocalDate.now().minusMonths(6));
            single.setTrainingEndDate(java.time.LocalDate.now().minusDays(1));
            single.setFeatureSetVersion("v1");
            return runForecast(vendorId, single);
        }).toList();
    }
}