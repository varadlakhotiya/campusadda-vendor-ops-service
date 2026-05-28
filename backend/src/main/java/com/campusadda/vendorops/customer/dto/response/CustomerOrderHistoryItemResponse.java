package com.campusadda.vendorops.customer.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerOrderHistoryItemResponse {
    private Long menuItemId;
    private String itemName;
    private Integer quantity;
}