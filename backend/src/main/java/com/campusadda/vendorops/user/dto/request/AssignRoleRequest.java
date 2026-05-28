package com.campusadda.vendorops.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRoleRequest {

    @NotBlank(message = "Role code is required")
    private String roleCode;
}