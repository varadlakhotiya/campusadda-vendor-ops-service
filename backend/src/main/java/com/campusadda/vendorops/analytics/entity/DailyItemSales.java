package com.campusadda.vendorops.analytics.entity;

import com.campusadda.vendorops.common.entity.AuditableEntity;
import com.campusadda.vendorops.etl.entity.EtlJobRun;
import com.campusadda.vendorops.menu.entity.MenuItem;
import com.campusadda.vendorops.vendor.entity.Vendor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "daily_item_sales")
public class DailyItemSales extends AuditableEntity {

    @Column(name = "sales_date", nullable = false)
    private LocalDate salesDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(name = "quantity_sold", nullable = false)
    private Integer quantitySold = 0;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount = 0;

    @Column(name = "gross_revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal grossRevenue = BigDecimal.ZERO;

    @Column(name = "net_revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal netRevenue = BigDecimal.ZERO;

    @Column(name = "avg_selling_price", precision = 10, scale = 2)
    private BigDecimal avgSellingPrice;

    @Column(name = "first_order_at")
    private LocalDateTime firstOrderAt;

    @Column(name = "last_order_at")
    private LocalDateTime lastOrderAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etl_run_id")
    private EtlJobRun etlRun;
}