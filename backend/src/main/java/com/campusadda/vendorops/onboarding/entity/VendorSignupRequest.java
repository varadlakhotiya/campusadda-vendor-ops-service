package com.campusadda.vendorops.onboarding.entity;

import com.campusadda.vendorops.common.entity.AuditableEntity;
import com.campusadda.vendorops.user.entity.User;
import com.campusadda.vendorops.vendor.entity.Vendor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vendor_signup_requests")
public class VendorSignupRequest extends AuditableEntity {

    @Column(name = "restaurant_name", nullable = false, length = 150)
    private String restaurantName;

    @Column(name = "contact_person_name", nullable = false, length = 150)
    private String contactPersonName;

    @Column(name = "contact_email", nullable = false, length = 150)
    private String contactEmail;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Column(name = "campus_area", length = 100)
    private String campusArea;

    @Column(name = "location_label", length = 150)
    private String locationLabel;

    @Column(name = "requested_role_code", nullable = false, length = 50)
    private String requestedRoleCode;

    @Column(name = "desired_password_hash", length = 255)
    private String desiredPasswordHash;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_vendor_id")
    private Vendor createdVendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_user_id")
    private User createdUser;
}