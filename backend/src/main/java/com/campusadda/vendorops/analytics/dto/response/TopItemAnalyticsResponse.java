package com.campusadda.vendorops.analytics.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TopItemAnalyticsResponse {
    private Long menuItemId;
    private String itemName;
    private Integer quantitySold;
    private BigDecimal grossRevenue;
}