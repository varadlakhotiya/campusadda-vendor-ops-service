package com.campusadda.vendorops.analytics.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RevenueSummaryResponse {
    private BigDecimal grossRevenue;
    private BigDecimal netRevenue;
    private BigDecimal avgOrderValue;
}