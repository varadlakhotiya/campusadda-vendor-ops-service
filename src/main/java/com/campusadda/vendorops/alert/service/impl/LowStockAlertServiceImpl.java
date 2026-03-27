package com.campusadda.vendorops.alert.service.impl;

import com.campusadda.vendorops.alert.entity.Alert;
import com.campusadda.vendorops.alert.repository.AlertRepository;
import com.campusadda.vendorops.alert.service.LowStockAlertService;
import com.campusadda.vendorops.common.enums.AlertSeverity;
import com.campusadda.vendorops.common.enums.AlertStatus;
import com.campusadda.vendorops.common.enums.AlertType;
import com.campusadda.vendorops.inventory.entity.InventoryItem;
import com.campusadda.vendorops.outbox.service.OutboxService; // ✅ NEW
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class LowStockAlertServiceImpl implements LowStockAlertService {

    private final AlertRepository alertRepository;

    private final OutboxService outboxService; // ✅ NEW

    @Override
    public void checkAndCreateLowStockAlert(InventoryItem inventoryItem) {
        if (inventoryItem.getCurrentQuantity().compareTo(inventoryItem.getLowStockThreshold()) > 0) {
            return;
        }

        boolean exists = alertRepository.existsByVendor_IdAndAlertTypeAndEntityTypeAndEntityIdAndStatus(
                inventoryItem.getVendor().getId(),
                AlertType.LOW_STOCK.name(),
                "INVENTORY_ITEM",
                inventoryItem.getId(),
                AlertStatus.OPEN.name()
        );

        if (exists) {
            return;
        }

        Alert alert = new Alert();
        alert.setVendor(inventoryItem.getVendor());
        alert.setAlertType(AlertType.LOW_STOCK.name());
        alert.setSeverity(AlertSeverity.WARNING.name());
        alert.setEntityType("INVENTORY_ITEM");
        alert.setEntityId(inventoryItem.getId());
        alert.setTitle("Low stock: " + inventoryItem.getItemName());
        alert.setMessage("Current stock of " + inventoryItem.getItemName()
                + " is " + inventoryItem.getCurrentQuantity()
                + " " + inventoryItem.getUnit()
                + ", below threshold " + inventoryItem.getLowStockThreshold());
        alert.setStatus(AlertStatus.OPEN.name());
        alert.setTriggeredAt(LocalDateTime.now());

        Alert savedAlert = alertRepository.save(alert);

        // ✅ OUTBOX EVENT ADDED
        outboxService.saveEvent(
                "ALERT",
                savedAlert.getId(),
                "LOW_STOCK_ALERT_CREATED",
                String.valueOf(savedAlert.getId()),
                "{\"alertId\":" + savedAlert.getId() +
                        ",\"inventoryItemId\":" + inventoryItem.getId() + "}"
        );
    }
}