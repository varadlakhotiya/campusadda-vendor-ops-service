package com.campusadda.vendorops.inventory.entity;

import com.campusadda.vendorops.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "inventory_policies")
public class InventoryPolicy extends AuditableEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(name = "lead_time_days", nullable = false)
    private Integer leadTimeDays = 1;

    @Column(name = "review_period_days", nullable = false)
    private Integer reviewPeriodDays = 1;

    @Column(name = "service_level_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal serviceLevelPct = new BigDecimal("95.00");

    @Column(name = "safety_stock_qty", precision = 14, scale = 3)
    private BigDecimal safetyStockQty;

    @Column(name = "reorder_point_qty", precision = 14, scale = 3)
    private BigDecimal reorderPointQty;

    @Column(name = "min_reorder_qty", precision = 14, scale = 3)
    private BigDecimal minReorderQty;

    @Column(name = "max_reorder_qty", precision = 14, scale = 3)
    private BigDecimal maxReorderQty;

    @Column(name = "preferred_model", length = 50)
    private String preferredModel;

    @Column(name = "auto_recommend_enabled", nullable = false)
    private Boolean autoRecommendEnabled = Boolean.TRUE;
}