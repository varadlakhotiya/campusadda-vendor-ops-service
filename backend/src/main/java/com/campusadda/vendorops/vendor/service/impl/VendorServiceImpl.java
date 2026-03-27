package com.campusadda.vendorops.vendor.service.impl;

import java.util.List;

import com.campusadda.vendorops.vendor.dto.request.CreateVendorRequest;
import com.campusadda.vendorops.vendor.dto.request.UpdateVendorRequest;
import com.campusadda.vendorops.vendor.dto.request.UpdateVendorStatusRequest;
import com.campusadda.vendorops.vendor.dto.response.VendorDetailResponse;
import com.campusadda.vendorops.vendor.dto.response.VendorResponse;
import com.campusadda.vendorops.vendor.dto.response.VendorSummaryResponse;
import com.campusadda.vendorops.vendor.dto.response.VendorUserAssignmentResponse;
import com.campusadda.vendorops.vendor.entity.Vendor;
import com.campusadda.vendorops.vendor.entity.VendorUserAssignment;
import com.campusadda.vendorops.vendor.mapper.VendorMapper;
import com.campusadda.vendorops.vendor.repository.VendorRepository;
import com.campusadda.vendorops.vendor.repository.VendorUserAssignmentRepository;
import com.campusadda.vendorops.vendor.service.VendorService;
import com.campusadda.vendorops.vendor.validator.VendorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;
    private final VendorUserAssignmentRepository vendorUserAssignmentRepository;
    private final VendorValidator vendorValidator;
    private final VendorMapper vendorMapper;

    @Override
    public VendorResponse createVendor(CreateVendorRequest request) {
        vendorValidator.validateVendorCodeUnique(request.getVendorCode());

        Vendor vendor = vendorMapper.toEntity(request);
        Vendor savedVendor = vendorRepository.save(vendor);

        return vendorMapper.toResponse(savedVendor);
    }

    @Override
    public VendorResponse updateVendor(Long vendorId, UpdateVendorRequest request) {
        Vendor vendor = vendorValidator.validateVendorExists(vendorId);

        vendorMapper.updateEntity(vendor, request);
        Vendor updatedVendor = vendorRepository.save(vendor);

        return vendorMapper.toResponse(updatedVendor);
    }

    @Override
    public VendorResponse updateVendorStatus(Long vendorId, UpdateVendorStatusRequest request) {
        Vendor vendor = vendorValidator.validateVendorExists(vendorId);
        vendor.setStatus(request.getStatus());

        Vendor updatedVendor = vendorRepository.save(vendor);
        return vendorMapper.toResponse(updatedVendor);
    }

    @Override
    @Transactional(readOnly = true)
    public VendorDetailResponse getVendorById(Long vendorId) {
        Vendor vendor = vendorValidator.validateVendorExists(vendorId);

        List<VendorUserAssignmentResponse> assignedUsers = vendorUserAssignmentRepository.findByVendor_Id(vendorId)
                .stream()
                .map(this::mapAssignmentResponse)
                .toList();

        return vendorMapper.toDetailResponse(vendor, assignedUsers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorResponse> getAllVendors() {
        return vendorRepository.findAll()
                .stream()
                .map(vendorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VendorSummaryResponse getVendorSummary(Long vendorId) {
        Vendor vendor = vendorValidator.validateVendorExists(vendorId);
        Long assignedUserCount = (long) vendorUserAssignmentRepository.findByVendor_Id(vendorId).size();

        return vendorMapper.toSummaryResponse(vendor, assignedUserCount);
    }

    private VendorUserAssignmentResponse mapAssignmentResponse(VendorUserAssignment assignment) {
        return VendorUserAssignmentResponse.builder()
                .id(assignment.getId())
                .vendorId(assignment.getVendor().getId())
                .vendorCode(assignment.getVendor().getVendorCode())
                .vendorName(assignment.getVendor().getName())
                .userId(assignment.getUser().getId())
                .userFullName(assignment.getUser().getFullName())
                .userEmail(assignment.getUser().getEmail())
                .assignmentRole(assignment.getAssignmentRole())
                .isPrimary(assignment.getIsPrimary())
                .status(assignment.getStatus())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}