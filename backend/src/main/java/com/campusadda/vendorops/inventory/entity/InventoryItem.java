package com.campusadda.vendorops.inventory.entity;

import com.campusadda.vendorops.common.entity.AuditableEntity;
import com.campusadda.vendorops.vendor.entity.Vendor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "inventory_items")
public class InventoryItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(name = "item_code", nullable = false, length = 40)
    private String itemCode;

    @Column(name = "item_name", nullable = false, length = 150)
    private String itemName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    @Column(name = "current_quantity", nullable = false, precision = 14, scale = 3)
    private BigDecimal currentQuantity = BigDecimal.ZERO;

    @Column(name = "reserved_quantity", nullable = false, precision = 14, scale = 3)
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Column(name = "low_stock_threshold", nullable = false, precision = 14, scale = 3)
    private BigDecimal lowStockThreshold = BigDecimal.ZERO;

    @Column(name = "max_stock_level", precision = 14, scale = 3)
    private BigDecimal maxStockLevel;

    @Column(name = "unit_cost", precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "last_restocked_at")
    private LocalDateTime lastRestockedAt;

    @Column(name = "source_system", nullable = false, length = 40)
    private String sourceSystem;

    @Column(name = "external_inventory_item_id", length = 64)
    private String externalInventoryItemId;
}