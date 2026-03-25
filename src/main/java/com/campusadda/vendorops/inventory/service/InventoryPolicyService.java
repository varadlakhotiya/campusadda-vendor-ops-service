package com.campusadda.vendorops.inventory.service;

import com.campusadda.vendorops.inventory.dto.request.UpsertInventoryPolicyRequest;
import com.campusadda.vendorops.inventory.dto.response.InventoryPolicyResponse;

public interface InventoryPolicyService {
    InventoryPolicyResponse upsertPolicy(Long vendorId, Long inventoryItemId, UpsertInventoryPolicyRequest request);
    InventoryPolicyResponse getPolicy(Long vendorId, Long inventoryItemId);
}