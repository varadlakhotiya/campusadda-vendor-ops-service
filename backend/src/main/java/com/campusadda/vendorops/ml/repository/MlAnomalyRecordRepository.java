package com.campusadda.vendorops.ml.repository;

import com.campusadda.vendorops.anomaly.entity.AnomalyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

import java.util.List;

public interface MlAnomalyRecordRepository extends JpaRepository<AnomalyRecord, Long> {
    List<AnomalyRecord>
findByVendor_IdAndStatusOrderByAnomalyDateDesc(
        Long vendorId,
        String status
);

List<AnomalyRecord>
findByVendor_IdAndStatusAndAnomalyDateGreaterThanEqualOrderByAnomalyDateDesc(
        Long vendorId,
        String status,
        LocalDate anomalyDate
);
}