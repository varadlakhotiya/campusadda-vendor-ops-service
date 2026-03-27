package com.campusadda.vendorops.etl.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EtlJobRunResponse {
    private Long id;
    private String jobName;
    private String runType;
    private String status;
    private Integer recordsProcessed;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}