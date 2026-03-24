package com.campusadda.vendorops.vendor.service;

import java.util.List;

import com.campusadda.vendorops.vendor.dto.request.CreateVendorRequest;
import com.campusadda.vendorops.vendor.dto.request.UpdateVendorRequest;
import com.campusadda.vendorops.vendor.dto.request.UpdateVendorStatusRequest;
import com.campusadda.vendorops.vendor.dto.response.VendorDetailResponse;
import com.campusadda.vendorops.vendor.dto.response.VendorResponse;
import com.campusadda.vendorops.vendor.dto.response.VendorSummaryResponse;

public interface VendorService {

    VendorResponse createVendor(CreateVendorRequest request);

    VendorResponse updateVendor(Long vendorId, UpdateVendorRequest request);

    VendorResponse updateVendorStatus(Long vendorId, UpdateVendorStatusRequest request);

    VendorDetailResponse getVendorById(Long vendorId);

    List<VendorResponse> getAllVendors();

    VendorSummaryResponse getVendorSummary(Long vendorId);
}