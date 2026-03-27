package com.campusadda.vendorops.inventory.service;

import com.campusadda.vendorops.inventory.dto.response.StockMovementResponse;

import java.util.List;

public interface StockMovementService {
    List<StockMovementResponse> getStockMovements(Long vendorId, Long inventoryItemId);
}