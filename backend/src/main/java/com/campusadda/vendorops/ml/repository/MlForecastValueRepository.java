package com.campusadda.vendorops.ml.repository;

import com.campusadda.vendorops.forecast.entity.ForecastValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MlForecastValueRepository extends JpaRepository<ForecastValue, Long> {
    List<ForecastValue> findByForecastRun_IdOrderByForecastDateAsc(Long forecastRunId);
}