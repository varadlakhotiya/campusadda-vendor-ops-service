package com.campusadda.vendorops.inventory.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.inventory.dto.request.UpsertInventoryPolicyRequest;
import com.campusadda.vendorops.inventory.dto.response.InventoryPolicyResponse;
import com.campusadda.vendorops.inventory.service.InventoryPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}/inventory-items/{inventoryItemId}/policy")
@RequiredArgsConstructor
public class InventoryPolicyController {

    private final InventoryPolicyService inventoryPolicyService;

    @PutMapping
    public ResponseEntity<ApiResponse<InventoryPolicyResponse>> upsertPolicy(
            @PathVariable Long vendorId,
            @PathVariable Long inventoryItemId,
            @Valid @RequestBody UpsertInventoryPolicyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Inventory policy saved successfully",
                inventoryPolicyService.upsertPolicy(vendorId, inventoryItemId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<InventoryPolicyResponse>> getPolicy(
            @PathVariable Long vendorId,
            @PathVariable Long inventoryItemId) {
        return ResponseEntity.ok(ApiResponse.success("Inventory policy fetched successfully",
                inventoryPolicyService.getPolicy(vendorId, inventoryItemId)));
    }
}