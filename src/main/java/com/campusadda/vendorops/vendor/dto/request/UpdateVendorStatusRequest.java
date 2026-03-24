package com.campusadda.vendorops.vendor.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateVendorStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;
}