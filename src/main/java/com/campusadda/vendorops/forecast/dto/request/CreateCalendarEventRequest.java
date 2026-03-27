package com.campusadda.vendorops.forecast.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateCalendarEventRequest {
    private LocalDate eventDate;
    private String eventType;
    private String title;
    private String description;
    private Integer impactLevel;
    private String campusArea;
    private Long vendorId;
    private Boolean isActive;
}