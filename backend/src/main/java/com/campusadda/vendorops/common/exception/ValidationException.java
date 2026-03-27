package com.campusadda.vendorops.common.exception;

import java.util.List;
import com.campusadda.vendorops.common.dto.ErrorResponse;

public class ValidationException extends RuntimeException {

    private final List<ErrorResponse.FieldErrorDetail> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = List.of();
    }

    public ValidationException(String message, List<ErrorResponse.FieldErrorDetail> errors) {
        super(message);
        this.errors = errors;
    }

    public List<ErrorResponse.FieldErrorDetail> getErrors() {
        return errors;
    }
}