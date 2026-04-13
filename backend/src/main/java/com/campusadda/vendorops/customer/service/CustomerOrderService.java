package com.campusadda.vendorops.customer.service;

import com.campusadda.vendorops.customer.dto.response.CustomerOrderDetailResponse;
import com.campusadda.vendorops.customer.dto.response.CustomerOrderHistoryResponse;

import java.util.List;

public interface CustomerOrderService {
    List<CustomerOrderHistoryResponse> getMyOrders();

    List<CustomerOrderHistoryResponse> getMyActiveOrders();

    CustomerOrderDetailResponse getMyOrder(Long orderId);
}