package com.campusadda.vendorops.menu.entity;

import com.campusadda.vendorops.common.entity.AuditableEntity;
import com.campusadda.vendorops.vendor.entity.Vendor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "menu_categories")
public class MenuCategory extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Column(name = "source_system", nullable = false, length = 40)
    private String sourceSystem;

    @Column(name = "external_category_id", length = 64)
    private String externalCategoryId;
}