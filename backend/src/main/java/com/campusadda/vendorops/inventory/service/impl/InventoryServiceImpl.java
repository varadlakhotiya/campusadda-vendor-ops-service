package com.campusadda.vendorops.inventory.service.impl;

import com.campusadda.vendorops.common.enums.MovementType;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.inventory.dto.request.*;
import com.campusadda.vendorops.inventory.dto.response.InventoryItemResponse;
import com.campusadda.vendorops.inventory.dto.response.LowStockItemResponse;
import com.campusadda.vendorops.inventory.entity.InventoryItem;
import com.campusadda.vendorops.inventory.entity.StockMovement;
import com.campusadda.vendorops.inventory.mapper.InventoryMapper;
import com.campusadda.vendorops.inventory.repository.InventoryItemRepository;
import com.campusadda.vendorops.inventory.repository.StockMovementRepository;
import com.campusadda.vendorops.inventory.service.InventoryService;
import com.campusadda.vendorops.inventory.validator.InventoryValidator;
import com.campusadda.vendorops.inventory.validator.StockOperationValidator;
import com.campusadda.vendorops.outbox.service.OutboxService; // ✅ NEW
import com.campusadda.vendorops.security.SecurityUtils;
import com.campusadda.vendorops.user.entity.User;
import com.campusadda.vendorops.user.repository.UserRepository;
import com.campusadda.vendorops.vendor.entity.Vendor;
import com.campusadda.vendorops.vendor.validator.VendorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InventoryValidator inventoryValidator;
    private final StockOperationValidator stockOperationValidator;
    private final InventoryMapper inventoryMapper;
    private final VendorValidator vendorValidator;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;

    private final OutboxService outboxService; // ✅ NEW

    @Override
    public InventoryItemResponse createInventoryItem(Long vendorId, CreateInventoryItemRequest request) {
        Vendor vendor = vendorValidator.validateVendorExists(vendorId);
        inventoryValidator.validateUniqueItemCode(vendorId, request.getItemCode());

        InventoryItem item = inventoryMapper.toEntity(request);
        item.setVendor(vendor);

        return inventoryMapper.toResponse(inventoryItemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getInventoryItems(Long vendorId) {
        vendorValidator.validateVendorExists(vendorId);
        return inventoryItemRepository.findByVendor_IdOrderByItemNameAsc(vendorId)
                .stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryItemResponse getInventoryItemById(Long vendorId, Long inventoryItemId) {
        return inventoryMapper.toResponse(inventoryValidator.validateInventoryItemExists(vendorId, inventoryItemId));
    }

    @Override
    public InventoryItemResponse updateInventoryItem(Long vendorId, Long inventoryItemId, UpdateInventoryItemRequest request) {
        InventoryItem item = inventoryValidator.validateInventoryItemExists(vendorId, inventoryItemId);
        inventoryMapper.updateEntity(item, request);
        return inventoryMapper.toResponse(inventoryItemRepository.save(item));
    }

    @Override
    public InventoryItemResponse updateInventoryItemStatus(Long vendorId, Long inventoryItemId, UpdateInventoryItemStatusRequest request) {
        InventoryItem item = inventoryValidator.validateInventoryItemExists(vendorId, inventoryItemId);
        item.setStatus(request.getStatus());
        return inventoryMapper.toResponse(inventoryItemRepository.save(item));
    }

    @Override
    public InventoryItemResponse stockIn(Long vendorId, Long inventoryItemId, StockInRequest request) {
        InventoryItem item = inventoryValidator.validateInventoryItemExists(vendorId, inventoryItemId);
        stockOperationValidator.validatePositiveQuantity(request.getQuantity());

        BigDecimal before = item.getCurrentQuantity();
        BigDecimal after = before.add(request.getQuantity());

        item.setCurrentQuantity(after);
        item.setLastRestockedAt(LocalDateTime.now());

        if (request.getUnitCost() != null) {
            item.setUnitCost(request.getUnitCost());
        }

        InventoryItem saved = inventoryItemRepository.save(item);
        saveMovement(saved, MovementType.IN.name(), request.getQuantity(), before, after, request.getUnitCost(), request.getReason());

        return inventoryMapper.toResponse(saved);
    }

    @Override
    public InventoryItemResponse stockOut(Long vendorId, Long inventoryItemId, StockOutRequest request) {
        InventoryItem item = inventoryValidator.validateInventoryItemExists(vendorId, inventoryItemId);
        stockOperationValidator.validatePositiveQuantity(request.getQuantity());
        stockOperationValidator.validateStockOutAllowed(item.getCurrentQuantity(), request.getQuantity());

        BigDecimal before = item.getCurrentQuantity();
        BigDecimal after = before.subtract(request.getQuantity());

        item.setCurrentQuantity(after);
        InventoryItem saved = inventoryItemRepository.save(item);
        saveMovement(saved, MovementType.OUT.name(), request.getQuantity().negate(), before, after, saved.getUnitCost(), request.getReason());

        return inventoryMapper.toResponse(saved);
    }

    @Override
    public InventoryItemResponse adjustStock(Long vendorId, Long inventoryItemId, StockAdjustmentRequest request) {
        InventoryItem item = inventoryValidator.validateInventoryItemExists(vendorId, inventoryItemId);

        BigDecimal before = item.getCurrentQuantity();
        BigDecimal after = request.getAdjustedQuantity();
        BigDecimal delta = after.subtract(before);

        item.setCurrentQuantity(after);
        InventoryItem saved = inventoryItemRepository.save(item);
        saveMovement(saved, MovementType.ADJUSTMENT.name(), delta, before, after, saved.getUnitCost(), request.getReason());

        return inventoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LowStockItemResponse> getLowStockItems(Long vendorId) {
        vendorValidator.validateVendorExists(vendorId);

        return inventoryItemRepository.findByVendor_IdOrderByItemNameAsc(vendorId)
                .stream()
                .filter(item -> item.getCurrentQuantity().compareTo(item.getLowStockThreshold()) <= 0)
                .map(inventoryMapper::toLowStockResponse)
                .toList();
    }

    private void saveMovement(
            InventoryItem item,
            String movementType,
            BigDecimal delta,
            BigDecimal before,
            BigDecimal after,
            BigDecimal unitCost,
            String reason) {

        Long currentUserId = securityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        StockMovement movement = new StockMovement();
        movement.setVendor(item.getVendor());
        movement.setInventoryItem(item);
        movement.setMovementType(movementType);
        movement.setQuantityDelta(delta);
        movement.setQuantityBefore(before);
        movement.setQuantityAfter(after);
        movement.setUnitCost(unitCost);
        movement.setReason(reason);
        movement.setCreatedByUser(currentUser);
        movement.setEventTime(LocalDateTime.now());

        stockMovementRepository.save(movement);

        // ✅ OUTBOX EVENT (ADDED)
        outboxService.saveEvent(
                "INVENTORY",
                item.getId(),
                "STOCK_UPDATED",
                item.getItemCode(),
                "{\"inventoryItemId\":" + item.getId() +
                        ",\"currentQuantity\":\"" + item.getCurrentQuantity() + "\"}"
        );
    }
}