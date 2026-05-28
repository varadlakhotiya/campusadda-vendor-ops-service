package com.campusadda.vendorops.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StockConsumptionPreviewResponse {
    private Long orderId;
    private Boolean canConsume;
    private List<String> issues;
}