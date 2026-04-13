package com.campusadda.vendorops.onboarding.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateVendorSignupRequest {
    private String restaurantName;
    private String contactPersonName;
    private String contactEmail;
    private String contactPhone;
    private String campusArea;
    private String locationLabel;
    private String password;
    private String notes;
}