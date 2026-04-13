package com.campusadda.vendorops.onboarding.service.impl;

import com.campusadda.vendorops.common.exception.ConflictException;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.onboarding.dto.request.ApproveVendorSignupRequest;
import com.campusadda.vendorops.onboarding.dto.request.CreateVendorSignupRequest;
import com.campusadda.vendorops.onboarding.dto.request.RejectVendorSignupRequest;
import com.campusadda.vendorops.onboarding.dto.response.VendorSignupRequestResponse;
import com.campusadda.vendorops.onboarding.entity.VendorSignupRequest;
import com.campusadda.vendorops.onboarding.repository.VendorSignupRequestRepository;
import com.campusadda.vendorops.security.SecurityUtils;
import com.campusadda.vendorops.user.entity.Role;
import com.campusadda.vendorops.user.entity.User;
import com.campusadda.vendorops.user.entity.UserRole;
import com.campusadda.vendorops.user.repository.RoleRepository;
import com.campusadda.vendorops.user.repository.UserRepository;
import com.campusadda.vendorops.user.repository.UserRoleRepository;
import com.campusadda.vendorops.vendor.entity.Vendor;
import com.campusadda.vendorops.vendor.entity.VendorUserAssignment;
import com.campusadda.vendorops.vendor.repository.VendorRepository;
import com.campusadda.vendorops.vendor.repository.VendorUserAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VendorSignupRequestServiceImpl implements com.campusadda.vendorops.onboarding.service.VendorSignupRequestService {

    private final VendorSignupRequestRepository signupRequestRepository;
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final VendorUserAssignmentRepository vendorUserAssignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @Override
    public VendorSignupRequestResponse create(CreateVendorSignupRequest request) {
        signupRequestRepository.findByContactEmail(request.getContactEmail())
                .ifPresent(existing -> {
                    if ("PENDING".equals(existing.getStatus()) || "APPROVED".equals(existing.getStatus())) {
                        throw new ConflictException("A signup request already exists for this email");
                    }
                });

        if (userRepository.findByEmail(request.getContactEmail()).isPresent()) {
            throw new ConflictException("A user already exists with this email");
        }

        VendorSignupRequest row = new VendorSignupRequest();
        row.setRestaurantName(request.getRestaurantName());
        row.setContactPersonName(request.getContactPersonName());
        row.setContactEmail(request.getContactEmail());
        row.setContactPhone(request.getContactPhone());
        row.setCampusArea(request.getCampusArea());
        row.setLocationLabel(request.getLocationLabel());
        row.setRequestedRoleCode("VENDOR_MANAGER");
        row.setDesiredPasswordHash(passwordEncoder.encode(request.getPassword()));
        row.setNotes(request.getNotes());
        row.setStatus("PENDING");

        return map(signupRequestRepository.save(row));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorSignupRequestResponse> listAll() {
        return signupRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorSignupRequestResponse> listPending() {
        return signupRequestRepository.findByStatusOrderByCreatedAtDesc("PENDING")
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public VendorSignupRequestResponse approve(Long requestId, ApproveVendorSignupRequest request) {
        VendorSignupRequest row = signupRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Signup request not found"));

        if (!"PENDING".equals(row.getStatus())) {
            throw new ConflictException("Only pending requests can be approved");
        }

        Role role = roleRepository.findByRoleCode(
                request.getRoleCode() != null ? request.getRoleCode() : "VENDOR_MANAGER"
        ).orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        Vendor vendor = vendorRepository.findByVendorCode(request.getVendorCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor not found with code: " + request.getVendorCode()
                ));

        User user = new User();
        user.setFullName(row.getContactPersonName());
        user.setEmail(row.getContactEmail());
        user.setPhone(row.getContactPhone());
        user.setPasswordHash(passwordEncoder.encode(
                request.getInitialPassword() != null && !request.getInitialPassword().isBlank()
                        ? request.getInitialPassword()
                        : "Vendor@123"
        ));
        user.setStatus("ACTIVE");
        user = userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);

        VendorUserAssignment assignment = new VendorUserAssignment();
        assignment.setVendor(vendor);
        assignment.setUser(user);
        assignment.setAssignmentRole(role.getRoleCode());
        assignment.setIsPrimary(Boolean.TRUE);
        assignment.setStatus("ACTIVE");
        vendorUserAssignmentRepository.save(assignment);

        Long reviewerId = securityUtils.getCurrentUserId();
        if (reviewerId != null) {
            row.setReviewedBy(userRepository.findById(reviewerId).orElse(null));
        }

        row.setReviewedAt(LocalDateTime.now());
        row.setStatus("APPROVED");
        row.setCreatedVendor(vendor);
        row.setCreatedUser(user);

        return map(signupRequestRepository.save(row));
    }

    @Override
    public VendorSignupRequestResponse reject(Long requestId, RejectVendorSignupRequest request) {
        VendorSignupRequest row = signupRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Signup request not found"));

        if (!"PENDING".equals(row.getStatus())) {
            throw new ConflictException("Only pending requests can be rejected");
        }

        Long reviewerId = securityUtils.getCurrentUserId();
        if (reviewerId != null) {
            row.setReviewedBy(userRepository.findById(reviewerId).orElse(null));
        }

        row.setReviewedAt(LocalDateTime.now());
        row.setStatus("REJECTED");
        row.setRejectionReason(request.getRejectionReason());

        return map(signupRequestRepository.save(row));
    }

    private VendorSignupRequestResponse map(VendorSignupRequest row) {
        return VendorSignupRequestResponse.builder()
                .id(row.getId())
                .restaurantName(row.getRestaurantName())
                .contactPersonName(row.getContactPersonName())
                .contactEmail(row.getContactEmail())
                .contactPhone(row.getContactPhone())
                .campusArea(row.getCampusArea())
                .locationLabel(row.getLocationLabel())
                .requestedRoleCode(row.getRequestedRoleCode())
                .status(row.getStatus())
                .rejectionReason(row.getRejectionReason())
                .createdVendorId(row.getCreatedVendor() != null ? row.getCreatedVendor().getId() : null)
                .createdUserId(row.getCreatedUser() != null ? row.getCreatedUser().getId() : null)
                .reviewedAt(row.getReviewedAt())
                .createdAt(row.getCreatedAt())
                .build();
    }
}