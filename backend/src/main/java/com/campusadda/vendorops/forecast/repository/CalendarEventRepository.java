package com.campusadda.vendorops.forecast.repository;

import com.campusadda.vendorops.forecast.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findByEventDateBetweenAndIsActiveTrue(LocalDate fromDate, LocalDate toDate);
}