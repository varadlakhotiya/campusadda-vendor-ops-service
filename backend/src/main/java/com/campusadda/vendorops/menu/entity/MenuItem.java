package com.campusadda.vendorops.menu.entity;

import com.campusadda.vendorops.common.entity.AuditableEntity;
import com.campusadda.vendorops.vendor.entity.Vendor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "menu_items")
public class MenuItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private MenuCategory category;

    @Column(name = "item_code", nullable = false, length = 40)
    private String itemCode;

    @Column(name = "item_name", nullable = false, length = 150)
    private String itemName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "prep_time_minutes")
    private Integer prepTimeMinutes;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = Boolean.TRUE;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Column(name = "is_veg", nullable = false)
    private Boolean isVeg = Boolean.TRUE;

    @Column(name = "track_inventory", nullable = false)
    private Boolean trackInventory = Boolean.TRUE;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "primary_image_url", length = 500)
    private String primaryImageUrl;

    @Column(name = "source_system", nullable = false, length = 40)
    private String sourceSystem;

    @Column(name = "external_menu_item_id", length = 64)
    private String externalMenuItemId;
}