package com.campusadda.vendorops.forecast.service.impl;

import com.campusadda.vendorops.forecast.dto.request.CreateCalendarEventRequest;
import com.campusadda.vendorops.forecast.dto.response.CalendarEventResponse;
import com.campusadda.vendorops.forecast.entity.CalendarEvent;
import com.campusadda.vendorops.forecast.repository.CalendarEventRepository;
import com.campusadda.vendorops.forecast.service.CalendarEventService;
import com.campusadda.vendorops.vendor.entity.Vendor;
import com.campusadda.vendorops.vendor.validator.VendorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CalendarEventServiceImpl implements CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;
    private final VendorValidator vendorValidator;

    @Override
    public CalendarEventResponse create(CreateCalendarEventRequest request) {
        CalendarEvent event = new CalendarEvent();
        event.setEventDate(request.getEventDate());
        event.setEventType(request.getEventType());
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());

        // ✅ FIXED: Integer → Byte conversion
        event.setImpactLevel(
                request.getImpactLevel() != null
                        ? request.getImpactLevel().byteValue()
                        : (byte) 1
        );

        event.setCampusArea(request.getCampusArea());
        event.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        if (request.getVendorId() != null) {
            Vendor vendor = vendorValidator.validateVendorExists(request.getVendorId());
            event.setVendor(vendor);
        }

        CalendarEvent saved = calendarEventRepository.save(event);
        return map(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> list() {
        return calendarEventRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    private CalendarEventResponse map(CalendarEvent event) {
        return CalendarEventResponse.builder()
                .id(event.getId())
                .eventDate(event.getEventDate())
                .eventType(event.getEventType())
                .title(event.getTitle())
                .description(event.getDescription())

                // ✅ FIXED: Byte → Integer conversion
                .impactLevel(event.getImpactLevel() != null ? event.getImpactLevel().intValue() : null)

                .campusArea(event.getCampusArea())
                .vendorId(event.getVendor() != null ? event.getVendor().getId() : null)
                .isActive(event.getIsActive())
                .build();
    }
}