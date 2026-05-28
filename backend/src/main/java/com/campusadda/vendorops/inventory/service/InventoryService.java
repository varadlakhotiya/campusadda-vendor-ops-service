package com.campusadda.vendorops.inventory.service;

import com.campusadda.vendorops.inventory.dto.request.*;
import com.campusadda.vendorops.inventory.dto.response.InventoryItemResponse;
import com.campusadda.vendorops.inventory.dto.response.LowStockItemResponse;

import java.util.List;

public interface InventoryService {
    InventoryItemResponse createInventoryItem(Long vendorId, CreateInventoryItemRequest request);
    List<InventoryItemResponse> getInventoryItems(Long vendorId);
    InventoryItemResponse getInventoryItemById(Long vendorId, Long inventoryItemId);
    InventoryItemResponse updateInventoryItem(Long vendorId, Long inventoryItemId, UpdateInventoryItemRequest request);
    InventoryItemResponse updateInventoryItemStatus(Long vendorId, Long inventoryItemId, UpdateInventoryItemStatusRequest request);
    InventoryItemResponse stockIn(Long vendorId, Long inventoryItemId, StockInRequest request);
    InventoryItemResponse stockOut(Long vendorId, Long inventoryItemId, StockOutRequest request);
    InventoryItemResponse adjustStock(Long vendorId, Long inventoryItemId, StockAdjustmentRequest request);
    List<LowStockItemResponse> getLowStockItems(Long vendorId);
}