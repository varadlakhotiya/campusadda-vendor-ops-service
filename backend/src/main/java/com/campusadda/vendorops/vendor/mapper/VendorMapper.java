package com.campusadda.vendorops.vendor.mapper;

import java.util.List;

import com.campusadda.vendorops.vendor.dto.request.CreateVendorRequest;
import com.campusadda.vendorops.vendor.dto.request.UpdateVendorRequest;
import com.campusadda.vendorops.vendor.dto.response.VendorDetailResponse;
import com.campusadda.vendorops.vendor.dto.response.VendorResponse;
import com.campusadda.vendorops.vendor.dto.response.VendorSummaryResponse;
import com.campusadda.vendorops.vendor.dto.response.VendorUserAssignmentResponse;
import com.campusadda.vendorops.vendor.entity.Vendor;
import org.springframework.stereotype.Component;

@Component
public class VendorMapper {

    public Vendor toEntity(CreateVendorRequest request) {
        Vendor vendor = new Vendor();
        vendor.setVendorCode(request.getVendorCode());
        vendor.setName(request.getName());
        vendor.setDescription(request.getDescription());
        vendor.setContactName(request.getContactName());
        vendor.setContactPhone(request.getContactPhone());
        vendor.setContactEmail(request.getContactEmail());
        vendor.setLocationLabel(request.getLocationLabel());
        vendor.setCampusArea(request.getCampusArea());
        vendor.setStatus(request.getStatus());
        vendor.setSourceSystem(request.getSourceSystem());
        vendor.setExternalVendorId(request.getExternalVendorId());
        return vendor;
    }

    public void updateEntity(Vendor vendor, UpdateVendorRequest request) {
        if (request.getName() != null) {
            vendor.setName(request.getName());
        }
        if (request.getDescription() != null) {
            vendor.setDescription(request.getDescription());
        }
        if (request.getContactName() != null) {
            vendor.setContactName(request.getContactName());
        }
        if (request.getContactPhone() != null) {
            vendor.setContactPhone(request.getContactPhone());
        }
        if (request.getContactEmail() != null) {
            vendor.setContactEmail(request.getContactEmail());
        }
        if (request.getLocationLabel() != null) {
            vendor.setLocationLabel(request.getLocationLabel());
        }
        if (request.getCampusArea() != null) {
            vendor.setCampusArea(request.getCampusArea());
        }
        if (request.getExternalVendorId() != null) {
            vendor.setExternalVendorId(request.getExternalVendorId());
        }
    }

    public VendorResponse toResponse(Vendor vendor) {
        if (vendor == null) {
            return null;
        }

        return VendorResponse.builder()
                .id(vendor.getId())
                .vendorCode(vendor.getVendorCode())
                .name(vendor.getName())
                .description(vendor.getDescription())
                .contactName(vendor.getContactName())
                .contactPhone(vendor.getContactPhone())
                .contactEmail(vendor.getContactEmail())
                .locationLabel(vendor.getLocationLabel())
                .campusArea(vendor.getCampusArea())
                .status(vendor.getStatus())
                .sourceSystem(vendor.getSourceSystem())
                .externalVendorId(vendor.getExternalVendorId())
                .createdAt(vendor.getCreatedAt())
                .updatedAt(vendor.getUpdatedAt())
                .build();
    }

    public VendorDetailResponse toDetailResponse(
            Vendor vendor,
            List<VendorUserAssignmentResponse> assignedUsers) {

        return VendorDetailResponse.builder()
                .id(vendor.getId())
                .vendorCode(vendor.getVendorCode())
                .name(vendor.getName())
                .description(vendor.getDescription())
                .contactName(vendor.getContactName())
                .contactPhone(vendor.getContactPhone())
                .contactEmail(vendor.getContactEmail())
                .locationLabel(vendor.getLocationLabel())
                .campusArea(vendor.getCampusArea())
                .status(vendor.getStatus())
                .sourceSystem(vendor.getSourceSystem())
                .externalVendorId(vendor.getExternalVendorId())
                .assignedUsers(assignedUsers)
                .createdAt(vendor.getCreatedAt())
                .updatedAt(vendor.getUpdatedAt())
                .build();
    }

    public VendorSummaryResponse toSummaryResponse(Vendor vendor, Long assignedUserCount) {
        return VendorSummaryResponse.builder()
                .vendorId(vendor.getId())
                .vendorCode(vendor.getVendorCode())
                .vendorName(vendor.getName())
                .status(vendor.getStatus())
                .assignedUserCount(assignedUserCount)
                .activeMenuItemCount(0L)
                .lowStockItemCount(0L)
                .todayOrderCount(0L)
                .todayRevenue(0.0)
                .build();
    }
}