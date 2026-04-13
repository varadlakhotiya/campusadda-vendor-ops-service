package com.campusadda.vendorops.onboarding.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectVendorSignupRequest {
    private String rejectionReason;
}