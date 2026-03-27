package com.campusadda.vendorops.inventory.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.inventory.dto.response.StockMovementResponse;
import com.campusadda.vendorops.inventory.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}/inventory-items/{inventoryItemId}/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getStockMovements(
            @PathVariable Long vendorId,
            @PathVariable Long inventoryItemId) {
        return ResponseEntity.ok(ApiResponse.success("Stock movements fetched successfully",
                stockMovementService.getStockMovements(vendorId, inventoryItemId)));
    }
}