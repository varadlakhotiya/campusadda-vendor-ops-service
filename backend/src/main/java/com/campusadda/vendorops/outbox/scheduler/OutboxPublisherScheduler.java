package com.campusadda.vendorops.outbox.scheduler;

import com.campusadda.vendorops.outbox.service.OutboxPublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxPublisherScheduler {

    private final OutboxPublisherService outboxPublisherService;

    @Scheduled(fixedDelayString = "${app.scheduler.outbox-publish-delay-ms:15000}")
    public void publishPendingEvents() {
        outboxPublisherService.publishPendingEvents();
    }
}