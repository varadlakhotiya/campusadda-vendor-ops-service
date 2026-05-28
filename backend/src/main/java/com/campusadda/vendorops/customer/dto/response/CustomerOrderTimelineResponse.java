package com.campusadda.vendorops.customer.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CustomerOrderTimelineResponse {
    private Long id;
    private String fromStatus;
    private String toStatus;
    private String remarks;
    private LocalDateTime changedAt;
}