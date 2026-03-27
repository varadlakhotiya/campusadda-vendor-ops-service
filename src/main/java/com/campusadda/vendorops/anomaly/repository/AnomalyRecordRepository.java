package com.campusadda.vendorops.anomaly.repository;

import com.campusadda.vendorops.anomaly.entity.AnomalyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnomalyRecordRepository extends JpaRepository<AnomalyRecord, Long> {
    List<AnomalyRecord> findByVendor_IdOrderByAnomalyDateDesc(Long vendorId);
}