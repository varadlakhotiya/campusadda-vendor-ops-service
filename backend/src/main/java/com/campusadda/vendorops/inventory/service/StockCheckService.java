package com.campusadda.vendorops.inventory.service;

import com.campusadda.vendorops.inventory.dto.response.AvailabilityCheckResponse;

public interface StockCheckService {
    AvailabilityCheckResponse checkMenuItemSellability(Long vendorId, Long menuItemId);
}