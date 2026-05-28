package com.campusadda.vendorops.alert.repository;

import com.campusadda.vendorops.alert.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByVendor_IdOrderByTriggeredAtDesc(Long vendorId);
    List<Alert> findByVendor_IdAndStatusOrderByTriggeredAtDesc(Long vendorId, String status);
    Optional<Alert> findByIdAndVendor_Id(Long id, Long vendorId);
    boolean existsByVendor_IdAndAlertTypeAndEntityTypeAndEntityIdAndStatus(
            Long vendorId, String alertType, String entityType, Long entityId, String status
    );
}