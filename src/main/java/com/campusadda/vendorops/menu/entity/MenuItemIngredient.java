package com.campusadda.vendorops.menu.entity;

import com.campusadda.vendorops.common.entity.AuditableEntity;
import com.campusadda.vendorops.inventory.entity.InventoryItem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "menu_item_ingredients")
public class MenuItemIngredient extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(name = "quantity_required", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantityRequired;

    @Column(name = "wastage_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal wastagePct = BigDecimal.ZERO;

    @Column(name = "is_optional", nullable = false)
    private Boolean isOptional = Boolean.FALSE;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;
}