package com.campusadda.vendorops.security;

public interface VendorAccessService {

    /**
     * Backward-compatible method.
     * Treat this as VIEW access.
     */
    void validateVendorAccess(Long vendorId);

    /**
     * Admin can view.
     * Assigned vendor users can view.
     */
    void validateVendorViewAccess(Long vendorId);

    /**
     * Only assigned vendor users can manage vendor workflow.
     * Admin is explicitly blocked from operational actions.
     */
    void validateVendorManageAccess(Long vendorId);
}