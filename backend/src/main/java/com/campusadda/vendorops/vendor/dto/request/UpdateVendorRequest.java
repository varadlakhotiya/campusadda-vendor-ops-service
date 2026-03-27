package com.campusadda.vendorops.vendor.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateVendorRequest {

    @Size(max = 150, message = "Vendor name must not exceed 150 characters")
    private String name;

    private String description;

    @Size(max = 120, message = "Contact name must not exceed 120 characters")
    private String contactName;

    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;

    @Email(message = "Contact email must be valid")
    @Size(max = 150, message = "Contact email must not exceed 150 characters")
    private String contactEmail;

    @Size(max = 150, message = "Location label must not exceed 150 characters")
    private String locationLabel;

    @Size(max = 100, message = "Campus area must not exceed 100 characters")
    private String campusArea;

    @Size(max = 64, message = "External vendor id must not exceed 64 characters")
    private String externalVendorId;
}