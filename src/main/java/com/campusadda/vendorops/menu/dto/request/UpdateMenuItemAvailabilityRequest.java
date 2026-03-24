package com.campusadda.vendorops.menu.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMenuItemAvailabilityRequest {
    @NotNull(message = "Availability flag is required")
    private Boolean isAvailable;
}