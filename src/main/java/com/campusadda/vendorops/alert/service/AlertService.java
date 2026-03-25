package com.campusadda.vendorops.alert.service;

import com.campusadda.vendorops.alert.dto.response.AlertResponse;

import java.util.List;

public interface AlertService {
    List<AlertResponse> getAlerts(Long vendorId);
    AlertResponse acknowledgeAlert(Long vendorId, Long alertId);
    AlertResponse resolveAlert(Long vendorId, Long alertId);
}