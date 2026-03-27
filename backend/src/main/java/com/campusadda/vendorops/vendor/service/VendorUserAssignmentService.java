package com.campusadda.vendorops.vendor.service;

import java.util.List;

import com.campusadda.vendorops.vendor.dto.request.AssignVendorUserRequest;
import com.campusadda.vendorops.vendor.dto.response.VendorUserAssignmentResponse;

public interface VendorUserAssignmentService {

    VendorUserAssignmentResponse assignUserToVendor(Long vendorId, AssignVendorUserRequest request);

    List<VendorUserAssignmentResponse> getVendorUsers(Long vendorId);

    List<VendorUserAssignmentResponse> getUserVendorAssignments(Long userId);

    void removeUserFromVendor(Long vendorId, Long userId);
}