package com.campusadda.vendorops.publicorder.service;

import com.campusadda.vendorops.publicorder.dto.request.CustomerCreateOrderRequest;
import com.campusadda.vendorops.publicorder.dto.response.CustomerOrderResponse;
import com.campusadda.vendorops.publicorder.dto.response.CustomerOrderSummaryResponse;
import com.campusadda.vendorops.publicorder.dto.response.CustomerOrderTrackingResponse;
import com.campusadda.vendorops.publicorder.dto.response.PublicVendorMenuResponse;
import com.campusadda.vendorops.publicorder.dto.response.PublicVendorResponse;

import java.util.List;

public interface PublicOrderingService {
    List<PublicVendorResponse> getActiveVendors();

    PublicVendorMenuResponse getVendorMenu(Long vendorId);

    CustomerOrderResponse placeOrder(CustomerCreateOrderRequest request);

    CustomerOrderTrackingResponse trackOrder(String orderNumber, String customerPhone);

    List<CustomerOrderSummaryResponse> getMyOrders();

    List<CustomerOrderSummaryResponse> getMyActiveOrders();

    CustomerOrderTrackingResponse getMyOrder(Long orderId);
}