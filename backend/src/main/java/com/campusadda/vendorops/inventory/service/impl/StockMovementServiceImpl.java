package com.campusadda.vendorops.inventory.service.impl;

import com.campusadda.vendorops.inventory.dto.response.StockMovementResponse;
import com.campusadda.vendorops.inventory.mapper.StockMovementMapper;
import com.campusadda.vendorops.inventory.repository.StockMovementRepository;
import com.campusadda.vendorops.inventory.service.StockMovementService;
import com.campusadda.vendorops.inventory.validator.InventoryValidator;
import com.campusadda.vendorops.security.VendorAccessService; // ✅ ADDED
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final InventoryValidator inventoryValidator;
    private final StockMovementMapper stockMovementMapper;
    private final VendorAccessService vendorAccessService; // ✅ ADDED

    @Override
    public List<StockMovementResponse> getStockMovements(Long vendorId, Long inventoryItemId) {

        // 🔐 Vendor access validation
        vendorAccessService.validateVendorAccess(vendorId);

        inventoryValidator.validateInventoryItemExists(vendorId, inventoryItemId);

        return stockMovementRepository.findByInventoryItem_IdOrderByEventTimeDesc(inventoryItemId)
                .stream()
                .map(stockMovementMapper::toResponse)
                .toList();
    }
}