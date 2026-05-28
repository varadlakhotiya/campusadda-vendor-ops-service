package com.campusadda.vendorops.alert.service.impl;

import com.campusadda.vendorops.alert.dto.response.AlertResponse;
import com.campusadda.vendorops.alert.entity.Alert;
import com.campusadda.vendorops.alert.mapper.AlertMapper;
import com.campusadda.vendorops.alert.repository.AlertRepository;
import com.campusadda.vendorops.alert.service.AlertService;
import com.campusadda.vendorops.alert.validator.AlertValidator;
import com.campusadda.vendorops.common.enums.AlertStatus;
import com.campusadda.vendorops.security.VendorAccessService; // ✅ ADD THIS
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final AlertValidator alertValidator;
    private final AlertMapper alertMapper;
    private final VendorAccessService vendorAccessService; // ✅ ADD THIS

    @Override
    @Transactional(readOnly = true)
    public List<AlertResponse> getAlerts(Long vendorId) {
        vendorAccessService.validateVendorAccess(vendorId); // 🔐 ADD HERE

        return alertRepository.findByVendor_IdOrderByTriggeredAtDesc(vendorId)
                .stream()
                .map(alertMapper::toResponse)
                .toList();
    }

    @Override
    public AlertResponse acknowledgeAlert(Long vendorId, Long alertId) {
        vendorAccessService.validateVendorAccess(vendorId); // 🔐 ADD HERE

        Alert alert = alertValidator.validateAlertExists(vendorId, alertId);
        alert.setStatus(AlertStatus.ACKNOWLEDGED.name());
        alert.setAcknowledgedAt(LocalDateTime.now());
        return alertMapper.toResponse(alertRepository.save(alert));
    }

    @Override
    public AlertResponse resolveAlert(Long vendorId, Long alertId) {
        vendorAccessService.validateVendorAccess(vendorId); // 🔐 ADD HERE

        Alert alert = alertValidator.validateAlertExists(vendorId, alertId);
        alert.setStatus(AlertStatus.RESOLVED.name());
        alert.setResolvedAt(LocalDateTime.now());
        return alertMapper.toResponse(alertRepository.save(alert));
    }
}