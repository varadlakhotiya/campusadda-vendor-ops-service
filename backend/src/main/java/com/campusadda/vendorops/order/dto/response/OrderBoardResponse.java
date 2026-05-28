package com.campusadda.vendorops.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderBoardResponse {
    private List<OrderResponse> created;
    private List<OrderResponse> accepted;
    private List<OrderResponse> preparing;
    private List<OrderResponse> ready;
    private List<OrderResponse> completed;
}