package com.campusadda.vendorops.outbox.service;

public interface OutboxPublisherService {
    void publishPendingEvents();
}