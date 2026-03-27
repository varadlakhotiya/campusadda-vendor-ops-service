package com.campusadda.vendorops.outbox.repository;

import com.campusadda.vendorops.outbox.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findTop50ByPublishStatusOrderByCreatedAtAsc(String publishStatus);
}