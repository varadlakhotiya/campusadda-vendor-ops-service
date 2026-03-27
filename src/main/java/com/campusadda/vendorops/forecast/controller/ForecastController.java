package com.campusadda.vendorops.forecast.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.forecast.dto.request.BulkRunForecastRequest;
import com.campusadda.vendorops.forecast.dto.request.RunForecastRequest;
import com.campusadda.vendorops.forecast.dto.response.ForecastRunResponse;
import com.campusadda.vendorops.forecast.dto.response.ForecastValueResponse;
import com.campusadda.vendorops.forecast.dto.response.LatestForecastResponse;
import com.campusadda.vendorops.forecast.service.ForecastQueryService;
import com.campusadda.vendorops.forecast.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;
    private final ForecastQueryService forecastQueryService;

    @PostMapping("/forecast-runs")
    public ResponseEntity<ApiResponse<ForecastRunResponse>> runForecast(
            @PathVariable Long vendorId,
            @RequestBody RunForecastRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Forecast run completed",
                forecastService.runForecast(vendorId, request)));
    }

    @PostMapping("/forecast-runs/bulk")
    public ResponseEntity<ApiResponse<List<ForecastRunResponse>>> runBulkForecast(
            @PathVariable Long vendorId,
            @RequestBody BulkRunForecastRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Bulk forecast completed",
                forecastService.runBulkForecast(vendorId, request)));
    }

    @GetMapping("/forecast-runs")
    public ResponseEntity<ApiResponse<List<ForecastRunResponse>>> getRuns(@PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success("Forecast runs fetched successfully",
                forecastQueryService.getForecastRuns(vendorId)));
    }

    @GetMapping("/forecast-runs/{forecastRunId}/values")
    public ResponseEntity<ApiResponse<List<ForecastValueResponse>>> getValues(
            @PathVariable Long vendorId,
            @PathVariable Long forecastRunId) {
        return ResponseEntity.ok(ApiResponse.success("Forecast values fetched successfully",
                forecastQueryService.getForecastValues(forecastRunId)));
    }

    @GetMapping("/menu-items/{menuItemId}/forecast/latest")
    public ResponseEntity<ApiResponse<LatestForecastResponse>> getLatestForecast(
            @PathVariable Long vendorId,
            @PathVariable Long menuItemId) {
        return ResponseEntity.ok(ApiResponse.success("Latest forecast fetched successfully",
                forecastQueryService.getLatestForecast(vendorId, menuItemId)));
    }
}