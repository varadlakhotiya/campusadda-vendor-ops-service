package com.campusadda.vendorops.forecast.client.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MlJobResponse {
    private Integer insertedForecastRuns;
    private Integer insertedForecastValues;
    private Integer insertedRecommendations;
    private Integer insertedAnomalies;
    private Integer selectedSeriesCount;
    private Integer selectedCategoryCount;
    private String registryPath;
    private String itemXgboostModelPath;
    private String categoryXgboostModelPath;
    private String message;
    private List<Map<String, Object>> selections;
    private List<Map<String, Object>> seriesResults;
}
