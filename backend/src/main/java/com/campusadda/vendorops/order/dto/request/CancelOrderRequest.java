package com.campusadda.vendorops.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelOrderRequest {
    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}