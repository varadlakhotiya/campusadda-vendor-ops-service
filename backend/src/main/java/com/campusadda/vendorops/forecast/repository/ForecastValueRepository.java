package com.campusadda.vendorops.forecast.repository;

import com.campusadda.vendorops.forecast.entity.ForecastValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForecastValueRepository extends JpaRepository<ForecastValue, Long> {
    List<ForecastValue> findByForecastRun_IdOrderByForecastDateAsc(Long forecastRunId);
}