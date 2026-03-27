package com.campusadda.vendorops.analytics.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class HourlySalesTrendResponse {
    private LocalDate salesDate;
    private Integer salesHour;
    private Integer totalOrders;
    private Integer itemsSoldQty;
    private BigDecimal revenue;
}