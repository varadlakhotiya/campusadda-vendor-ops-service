package com.campusadda.vendorops.vendor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignVendorUserRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotBlank(message = "Assignment role is required")
    private String assignmentRole;

    @NotNull(message = "Primary flag is required")
    private Boolean isPrimary;
}