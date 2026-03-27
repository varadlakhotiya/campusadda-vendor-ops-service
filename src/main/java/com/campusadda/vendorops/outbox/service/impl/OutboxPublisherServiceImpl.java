package com.campusadda.vendorops.outbox.service.impl;

import com.campusadda.vendorops.outbox.entity.OutboxEvent;
import com.campusadda.vendorops.outbox.producer.KafkaEventProducer;
import com.campusadda.vendorops.outbox.repository.OutboxEventRepository;
import com.campusadda.vendorops.outbox.service.OutboxPublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OutboxPublisherServiceImpl implements OutboxPublisherService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaEventProducer kafkaEventProducer;

    @Value("${app.kafka.topics.order-created}")
    private String orderCreatedTopic;

    @Value("${app.kafka.topics.order-status-updated}")
    private String orderStatusUpdatedTopic;

    @Value("${app.kafka.topics.stock-updated}")
    private String stockUpdatedTopic;

    @Value("${app.kafka.topics.low-stock-alert-created}")
    private String lowStockAlertCreatedTopic;

    @Override
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findTop50ByPublishStatusOrderByCreatedAtAsc("PENDING");

        for (OutboxEvent event : pendingEvents) {
            try {
                String topic = resolveTopic(event.getEventType());
                kafkaEventProducer.publish(topic, event.getEventKey(), event.getPayloadJson());

                event.setPublishStatus("PUBLISHED");
                event.setPublishedAt(LocalDateTime.now());
                event.setLastError(null);
            } catch (Exception ex) {
                event.setPublishStatus("FAILED");
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(ex.getMessage());
            }

            outboxEventRepository.save(event);
        }
    }

    private String resolveTopic(String eventType) {
        return switch (eventType) {
            case "ORDER_CREATED" -> orderCreatedTopic;
            case "ORDER_STATUS_UPDATED" -> orderStatusUpdatedTopic;
            case "STOCK_UPDATED" -> stockUpdatedTopic;
            case "LOW_STOCK_ALERT_CREATED" -> lowStockAlertCreatedTopic;
            default -> "generic-events";
        };
    }
}