package com.campusadda.vendorops.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateInventoryItemStatusRequest {
    @NotBlank(message = "Status is required")
    private String status;
}