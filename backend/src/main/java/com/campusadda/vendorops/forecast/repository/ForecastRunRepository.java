package com.campusadda.vendorops.forecast.repository;

import com.campusadda.vendorops.forecast.entity.ForecastRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForecastRunRepository extends JpaRepository<ForecastRun, Long> {
    List<ForecastRun> findByVendor_IdOrderByStartedAtDesc(Long vendorId);
    List<ForecastRun> findByVendor_IdAndMenuItem_IdOrderByStartedAtDesc(Long vendorId, Long menuItemId);
}