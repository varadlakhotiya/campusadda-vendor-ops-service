package com.campusadda.vendorops.forecast.service;

import com.campusadda.vendorops.forecast.dto.request.CreateCalendarEventRequest;
import com.campusadda.vendorops.forecast.dto.response.CalendarEventResponse;

import java.util.List;

public interface CalendarEventService {
    CalendarEventResponse create(CreateCalendarEventRequest request);
    List<CalendarEventResponse> list();
}