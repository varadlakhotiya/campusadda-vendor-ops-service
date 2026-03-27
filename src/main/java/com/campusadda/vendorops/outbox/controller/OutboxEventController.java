package com.campusadda.vendorops.outbox.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events/outbox")
@RequiredArgsConstructor
public class OutboxEventController {

    private final OutboxEventRepository outboxEventRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> list() {
        return ResponseEntity.ok(ApiResponse.success("Outbox events fetched successfully",
                outboxEventRepository.findAll()));
    }
}