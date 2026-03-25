package com.campusadda.vendorops.alert.validator;

import com.campusadda.vendorops.alert.entity.Alert;
import com.campusadda.vendorops.alert.repository.AlertRepository;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlertValidator {

    private final AlertRepository alertRepository;

    public Alert validateAlertExists(Long vendorId, Long alertId) {
        return alertRepository.findByIdAndVendor_Id(alertId, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
    }
}