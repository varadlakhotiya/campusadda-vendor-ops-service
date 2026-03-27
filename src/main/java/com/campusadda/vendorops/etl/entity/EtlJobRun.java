package com.campusadda.vendorops.etl.entity;

import com.campusadda.vendorops.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "etl_job_runs")
public class EtlJobRun extends AuditableEntity {

    @Column(name = "job_name", nullable = false, length = 100)
    private String jobName;

    @Column(name = "run_type", nullable = false, length = 32)
    private String runType;

    @Column(name = "window_start", nullable = false)
    private LocalDateTime windowStart;

    @Column(name = "window_end", nullable = false)
    private LocalDateTime windowEnd;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "records_processed", nullable = false)
    private Integer recordsProcessed = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
}