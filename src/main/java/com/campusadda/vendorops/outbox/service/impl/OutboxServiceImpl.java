package com.campusadda.vendorops.outbox.service.impl;

import com.campusadda.vendorops.outbox.entity.OutboxEvent;
import com.campusadda.vendorops.outbox.repository.OutboxEventRepository;
import com.campusadda.vendorops.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OutboxServiceImpl implements OutboxService {

    private final OutboxEventRepository outboxEventRepository;

    @Override
    public void saveEvent(String aggregateType, Long aggregateId, String eventType, String eventKey, String payloadJson) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setEventKey(eventKey);
        event.setPayloadJson(payloadJson);
        event.setPublishStatus("PENDING");
        event.setRetryCount(0);

        outboxEventRepository.save(event);
    }
}