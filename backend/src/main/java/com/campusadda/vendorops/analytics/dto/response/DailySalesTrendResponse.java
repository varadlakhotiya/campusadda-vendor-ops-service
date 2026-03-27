package com.campusadda.vendorops.analytics.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class DailySalesTrendResponse {
    private LocalDate salesDate;
    private Integer totalOrders;
    private BigDecimal grossRevenue;
    private BigDecimal netRevenue;
}