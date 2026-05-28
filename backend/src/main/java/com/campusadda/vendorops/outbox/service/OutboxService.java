package com.campusadda.vendorops.outbox.service;

public interface OutboxService {
    void saveEvent(String aggregateType, Long aggregateId, String eventType, String eventKey, String payloadJson);
}