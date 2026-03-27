package com.campusadda.vendorops.forecast.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class CalendarEventResponse {
    private Long id;
    private LocalDate eventDate;
    private String eventType;
    private String title;
    private String description;
    private Integer impactLevel;
    private String campusArea;
    private Long vendorId;
    private Boolean isActive;
}