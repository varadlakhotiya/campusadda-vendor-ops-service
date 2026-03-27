package com.campusadda.vendorops.alert.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AlertResponse {
    private Long id;
    private Long vendorId;
    private String alertType;
    private String severity;
    private String entityType;
    private Long entityId;
    private String title;
    private String message;
    private String status;
    private String metadataJson;
    private LocalDateTime triggeredAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}