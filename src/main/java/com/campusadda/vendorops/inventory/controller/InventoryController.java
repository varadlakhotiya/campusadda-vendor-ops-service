package com.campusadda.vendorops.inventory.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.inventory.dto.request.*;
import com.campusadda.vendorops.inventory.dto.response.InventoryItemResponse;
import com.campusadda.vendorops.inventory.dto.response.LowStockItemResponse;
import com.campusadda.vendorops.inventory.service.InventoryService;
import com.campusadda.vendorops.inventory.service.StockCheckService;
import com.campusadda.vendorops.inventory.dto.response.AvailabilityCheckResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final StockCheckService stockCheckService;

    @PostMapping("/inventory-items")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> createInventoryItem(
            @PathVariable Long vendorId,
            @Valid @RequestBody CreateInventoryItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inventory item created successfully",
                        inventoryService.createInventoryItem(vendorId, request)));
    }

    @GetMapping("/inventory-items")
    public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> getInventoryItems(@PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success("Inventory items fetched successfully",
                inventoryService.getInventoryItems(vendorId)));
    }

    @GetMapping("/inventory-items/{inventoryItemId}")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> getInventoryItem(
            @PathVariable Long vendorId,
            @PathVariable Long inventoryItemId) {
        return ResponseEntity.ok(ApiResponse.success("Inventory item fetched successfully",
                inventoryService.getInventoryItemById(vendorId, inventoryItemId)));
    }

    @PutMapping("/inventory-items/{inventoryItemId}")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> updateInventoryItem(
            @PathVariable Long vendorId,
            @PathVariable Long inventoryItemId,
            @Valid @RequestBody UpdateInventoryItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Inventory item updated successfully",
                inventoryService.updateInventoryItem(vendorId, inventoryItemId, request)));
    }

    @PatchMapping("/inventory-items/{inventoryItemId}/status")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> updateInventoryStatus(
            @PathVariable Long vendorId,
            @PathVariable Long inventoryItemId,
            @Valid @RequestBody UpdateInventoryItemStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Inventory item status updated successfully",
                inventoryService.updateInventoryItemStatus(vendorId, inventoryItemId, request)));
    }

    @PostMapping("/inventory-items/{inventoryItemId}/stock-in")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> stockIn(
            @PathVariable Long vendorId,
            @PathVariable Long inventoryItemId,
            @Valid @RequestBody StockInRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock added successfully",
                inventoryService.stockIn(vendorId, inventoryItemId, request)));
    }

    @PostMapping("/inventory-items/{inventoryItemId}/stock-out")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> stockOut(
            @PathVariable Long vendorId,
            @PathVariable Long inventoryItemId,
            @Valid @RequestBody StockOutRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock deducted successfully",
                inventoryService.stockOut(vendorId, inventoryItemId, request)));
    }

    @PostMapping("/inventory-items/{inventoryItemId}/adjustments")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> adjustStock(
            @PathVariable Long vendorId,
            @PathVariable Long inventoryItemId,
            @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted successfully",
                inventoryService.adjustStock(vendorId, inventoryItemId, request)));
    }

    @GetMapping("/inventory-items/low-stock")
    public ResponseEntity<ApiResponse<List<LowStockItemResponse>>> getLowStockItems(
            @PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success("Low stock items fetched successfully",
                inventoryService.getLowStockItems(vendorId)));
    }

    @GetMapping("/menu-items/{menuItemId}/availability-check")
    public ResponseEntity<ApiResponse<AvailabilityCheckResponse>> availabilityCheck(
            @PathVariable Long vendorId,
            @PathVariable Long menuItemId) {
        return ResponseEntity.ok(ApiResponse.success("Availability check completed",
                stockCheckService.checkMenuItemSellability(vendorId, menuItemId)));
    }
}