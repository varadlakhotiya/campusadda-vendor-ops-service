package com.campusadda.vendorops.alert.service;

import com.campusadda.vendorops.inventory.entity.InventoryItem;

public interface LowStockAlertService {
    void checkAndCreateLowStockAlert(InventoryItem inventoryItem);
}