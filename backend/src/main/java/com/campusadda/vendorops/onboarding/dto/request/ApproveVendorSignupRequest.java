package com.campusadda.vendorops.onboarding.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveVendorSignupRequest {
    private String vendorCode;
    private String roleCode = "VENDOR_MANAGER";
    private String initialPassword;
}