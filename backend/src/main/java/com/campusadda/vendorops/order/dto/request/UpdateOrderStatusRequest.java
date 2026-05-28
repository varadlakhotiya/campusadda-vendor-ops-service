package com.campusadda.vendorops.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderStatusRequest {
    @NotBlank(message = "Status is required")
    private String status;
    private String remarks;
}