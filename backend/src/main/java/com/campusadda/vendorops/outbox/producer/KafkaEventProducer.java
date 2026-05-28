package com.campusadda.vendorops.outbox.producer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "app.kafka.enabled",
    havingValue = "true"
)
public class KafkaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload);
    }
}