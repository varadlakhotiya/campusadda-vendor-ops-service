package com.campusadda.vendorops.vendor.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VendorSummaryResponse {

    private Long vendorId;
    private String vendorCode;
    private String vendorName;
    private String status;

    // Phase 1 placeholders
    private Long assignedUserCount;

    // Future dashboard fields
    private Long activeMenuItemCount;
    private Long lowStockItemCount;
    private Long todayOrderCount;
    private Double todayRevenue;
}