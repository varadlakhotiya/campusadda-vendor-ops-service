package com.campusadda.vendorops.onboarding.service;

import com.campusadda.vendorops.onboarding.dto.request.ApproveVendorSignupRequest;
import com.campusadda.vendorops.onboarding.dto.request.CreateVendorSignupRequest;
import com.campusadda.vendorops.onboarding.dto.request.RejectVendorSignupRequest;
import com.campusadda.vendorops.onboarding.dto.response.VendorSignupRequestResponse;

import java.util.List;

public interface VendorSignupRequestService {
    VendorSignupRequestResponse create(CreateVendorSignupRequest request);
    List<VendorSignupRequestResponse> listAll();
    List<VendorSignupRequestResponse> listPending();
    VendorSignupRequestResponse approve(Long requestId, ApproveVendorSignupRequest request);
    VendorSignupRequestResponse reject(Long requestId, RejectVendorSignupRequest request);
}