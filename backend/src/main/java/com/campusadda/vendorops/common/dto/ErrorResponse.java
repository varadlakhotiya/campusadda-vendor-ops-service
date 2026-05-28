package com.campusadda.vendorops.common.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {

    private final boolean success;
    private final String message;
    private final List<FieldErrorDetail> errors;
    private final LocalDateTime timestamp;
    private final String path;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class FieldErrorDetail {
        private final String field;
        private final String message;
    }
}