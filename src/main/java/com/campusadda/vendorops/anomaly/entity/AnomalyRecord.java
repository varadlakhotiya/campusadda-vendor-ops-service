package com.campusadda.vendorops.anomaly.entity;

import com.campusadda.vendorops.common.entity.AuditableEntity;
import com.campusadda.vendorops.inventory.entity.InventoryItem;
import com.campusadda.vendorops.menu.entity.MenuItem;
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
@Table(name = "anomaly_records")
public class AnomalyRecord extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id")
    private InventoryItem inventoryItem;

    @Column(name = "anomaly_date", nullable = false)
    private LocalDate anomalyDate;

    @Column(name = "anomaly_type", nullable = false, length = 40)
    private String anomalyType;

    @Column(name = "observed_value", nullable = false, precision = 14, scale = 3)
    private BigDecimal observedValue;

    @Column(name = "expected_value", precision = 14, scale = 3)
    private BigDecimal expectedValue;

    @Column(name = "deviation_score", precision = 14, scale = 4)
    private BigDecimal deviationScore;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "details_json", columnDefinition = "json")
    private String detailsJson;
}