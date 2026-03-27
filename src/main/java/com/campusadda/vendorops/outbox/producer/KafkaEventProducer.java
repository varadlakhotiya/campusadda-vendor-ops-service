package com.campusadda.vendorops.outbox.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload);
    }
}