package com.campusadda.vendorops.onboarding.repository;

import com.campusadda.vendorops.onboarding.entity.VendorSignupRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorSignupRequestRepository extends JpaRepository<VendorSignupRequest, Long> {
    Optional<VendorSignupRequest> findByContactEmail(String contactEmail);
    List<VendorSignupRequest> findByStatusOrderByCreatedAtDesc(String status);
    List<VendorSignupRequest> findAllByOrderByCreatedAtDesc();
}