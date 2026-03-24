package com.campusadda.vendorops.inventory.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AvailabilityCheckResponse {
    private Long menuItemId;
    private String menuItemName;
    private Boolean sellable;
    private List<String> issues;
}