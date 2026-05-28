package com.campusadda.vendorops.forecast.entity;

import com.campusadda.vendorops.common.entity.AuditableEntity;
import com.campusadda.vendorops.inventory.entity.InventoryItem;
import com.campusadda.vendorops.vendor.entity.Vendor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reorder_recommendations")
public class ReorderRecommendation extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forecast_run_id")
    private ForecastRun forecastRun;

    @Column(name = "recommendation_date", nullable = false)
    private LocalDate recommendationDate;

    @Column(name = "current_stock_qty", nullable = false, precision = 14, scale = 3)
    private BigDecimal currentStockQty;

    @Column(name = "lead_time_days", nullable = false)
    private Integer leadTimeDays;

    @Column(name = "forecast_demand_qty", nullable = false, precision = 14, scale = 3)
    private BigDecimal forecastDemandQty;

    @Column(name = "safety_stock_qty", nullable = false, precision = 14, scale = 3)
    private BigDecimal safetyStockQty;

    @Column(name = "reorder_point_qty", nullable = false, precision = 14, scale = 3)
    private BigDecimal reorderPointQty;

    @Column(name = "suggested_reorder_qty", nullable = false, precision = 14, scale = 3)
    private BigDecimal suggestedReorderQty;

    @Column(name = "recommendation_status", nullable = false, length = 32)
    private String recommendationStatus;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;
}