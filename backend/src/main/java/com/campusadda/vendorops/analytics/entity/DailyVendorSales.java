package com.campusadda.vendorops.analytics.entity;

import com.campusadda.vendorops.common.entity.AuditableEntity;
import com.campusadda.vendorops.etl.entity.EtlJobRun;
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
@Table(name = "daily_vendor_sales")
public class DailyVendorSales extends AuditableEntity {

    @Column(name = "sales_date", nullable = false)
    private LocalDate salesDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(name = "total_orders", nullable = false)
    private Integer totalOrders = 0;

    @Column(name = "completed_orders", nullable = false)
    private Integer completedOrders = 0;

    @Column(name = "cancelled_orders", nullable = false)
    private Integer cancelledOrders = 0;

    @Column(name = "items_sold_qty", nullable = false)
    private Integer itemsSoldQty = 0;

    @Column(name = "gross_revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal grossRevenue = BigDecimal.ZERO;

    @Column(name = "net_revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal netRevenue = BigDecimal.ZERO;

    @Column(name = "avg_order_value", precision = 10, scale = 2)
    private BigDecimal avgOrderValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etl_run_id")
    private EtlJobRun etlRun;
}