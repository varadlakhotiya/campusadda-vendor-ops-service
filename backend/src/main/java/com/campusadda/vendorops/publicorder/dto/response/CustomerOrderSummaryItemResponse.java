package com.campusadda.vendorops.publicorder.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerOrderSummaryItemResponse {
    private Long id;
    private String itemName;
    private Integer quantity;
}