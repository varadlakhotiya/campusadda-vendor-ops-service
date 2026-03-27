package com.campusadda.vendorops.inventory.service.impl;

import com.campusadda.vendorops.inventory.dto.request.UpsertInventoryPolicyRequest;
import com.campusadda.vendorops.inventory.dto.response.InventoryPolicyResponse;
import com.campusadda.vendorops.inventory.entity.InventoryItem;
import com.campusadda.vendorops.inventory.entity.InventoryPolicy;
import com.campusadda.vendorops.inventory.mapper.InventoryMapper;
import com.campusadda.vendorops.inventory.repository.InventoryPolicyRepository;
import com.campusadda.vendorops.inventory.service.InventoryPolicyService;
import com.campusadda.vendorops.inventory.validator.InventoryValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryPolicyServiceImpl implements InventoryPolicyService {

    private final InventoryPolicyRepository inventoryPolicyRepository;
    private final InventoryValidator inventoryValidator;
    private final InventoryMapper inventoryMapper;

    @Override
    public InventoryPolicyResponse upsertPolicy(Long vendorId, Long inventoryItemId, UpsertInventoryPolicyRequest request) {
        InventoryItem item = inventoryValidator.validateInventoryItemExists(vendorId, inventoryItemId);

        InventoryPolicy policy = inventoryPolicyRepository.findByInventoryItem_Id(item.getId())
                .orElseGet(() -> {
                    InventoryPolicy p = new InventoryPolicy();
                    p.setInventoryItem(item);
                    return p;
                });

        if (request.getLeadTimeDays() != null) policy.setLeadTimeDays(request.getLeadTimeDays());
        if (request.getReviewPeriodDays() != null) policy.setReviewPeriodDays(request.getReviewPeriodDays());
        if (request.getServiceLevelPct() != null) policy.setServiceLevelPct(request.getServiceLevelPct());
        if (request.getSafetyStockQty() != null) policy.setSafetyStockQty(request.getSafetyStockQty());
        if (request.getReorderPointQty() != null) policy.setReorderPointQty(request.getReorderPointQty());
        if (request.getMinReorderQty() != null) policy.setMinReorderQty(request.getMinReorderQty());
        if (request.getMaxReorderQty() != null) policy.setMaxReorderQty(request.getMaxReorderQty());
        if (request.getPreferredModel() != null) policy.setPreferredModel(request.getPreferredModel());
        if (request.getAutoRecommendEnabled() != null) policy.setAutoRecommendEnabled(request.getAutoRecommendEnabled());

        return inventoryMapper.toPolicyResponse(inventoryPolicyRepository.save(policy));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryPolicyResponse getPolicy(Long vendorId, Long inventoryItemId) {
        InventoryItem item = inventoryValidator.validateInventoryItemExists(vendorId, inventoryItemId);
        InventoryPolicy policy = inventoryPolicyRepository.findByInventoryItem_Id(item.getId())
                .orElseThrow(() -> new com.campusadda.vendorops.common.exception.ResourceNotFoundException("Inventory policy not found"));

        return inventoryMapper.toPolicyResponse(policy);
    }
}