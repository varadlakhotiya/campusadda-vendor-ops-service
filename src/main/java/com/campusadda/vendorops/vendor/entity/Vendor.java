package com.campusadda.vendorops.vendor.entity;

import com.campusadda.vendorops.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vendors")
public class Vendor extends AuditableEntity {

    @Column(name = "vendor_code", nullable = false, unique = true, length = 40)
    private String vendorCode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "contact_name", length = 120)
    private String contactName;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Column(name = "location_label", length = 150)
    private String locationLabel;

    @Column(name = "campus_area", length = 100)
    private String campusArea;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "source_system", nullable = false, length = 40)
    private String sourceSystem;

    @Column(name = "external_vendor_id", length = 64)
    private String externalVendorId;
}