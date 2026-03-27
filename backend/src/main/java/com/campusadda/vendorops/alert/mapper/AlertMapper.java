package com.campusadda.vendorops.alert.mapper;

import com.campusadda.vendorops.alert.dto.response.AlertResponse;
import com.campusadda.vendorops.alert.entity.Alert;
import org.springframework.stereotype.Component;

@Component
public class AlertMapper {

    public AlertResponse toResponse(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .vendorId(alert.getVendor().getId())
                .alertType(alert.getAlertType())
                .severity(alert.getSeverity())
                .entityType(alert.getEntityType())
                .entityId(alert.getEntityId())
                .title(alert.getTitle())
                .message(alert.getMessage())
                .status(alert.getStatus())
                .metadataJson(alert.getMetadataJson())
                .triggeredAt(alert.getTriggeredAt())
                .acknowledgedAt(alert.getAcknowledgedAt())
                .resolvedAt(alert.getResolvedAt())
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .build();
    }
}