package com.campusadda.vendorops.forecast.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.forecast.dto.request.CreateCalendarEventRequest;
import com.campusadda.vendorops.forecast.dto.response.CalendarEventResponse;
import com.campusadda.vendorops.forecast.service.CalendarEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/calendar-events")
@RequiredArgsConstructor
public class CalendarEventController {

    private final CalendarEventService calendarEventService;

    @PostMapping
    public ResponseEntity<ApiResponse<CalendarEventResponse>> create(@RequestBody CreateCalendarEventRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Calendar event created successfully",
                calendarEventService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CalendarEventResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success("Calendar events fetched successfully",
                calendarEventService.list()));
    }
}