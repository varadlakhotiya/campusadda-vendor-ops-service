package com.campusadda.vendorops.order.service;

import com.campusadda.vendorops.menu.entity.MenuItem;
import com.campusadda.vendorops.order.dto.request.CreateOrderRequest;

import java.util.Map;

public interface OrderValidationService {
    Map<Long, MenuItem> validateAndLoadMenuItems(CreateOrderRequest request);
}