package com.campusadda.vendorops.vendor.validator;

import com.campusadda.vendorops.common.exception.ConflictException;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.user.entity.User;
import com.campusadda.vendorops.user.repository.UserRepository;
import com.campusadda.vendorops.vendor.entity.Vendor;
import com.campusadda.vendorops.vendor.repository.VendorRepository;
import com.campusadda.vendorops.vendor.repository.VendorUserAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VendorValidator {

    private final VendorRepository vendorRepository;
    private final VendorUserAssignmentRepository vendorUserAssignmentRepository;
    private final UserRepository userRepository;

    public void validateVendorCodeUnique(String vendorCode) {
        if (vendorCode != null && vendorRepository.existsByVendorCode(vendorCode)) {
            throw new ConflictException("Vendor code already exists");
        }
    }

    public Vendor validateVendorExists(Long vendorId) {
        return vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + vendorId));
    }

    public User validateUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public void validateVendorUserNotAlreadyAssigned(Long vendorId, Long userId) {
        if (vendorUserAssignmentRepository.existsByVendor_IdAndUser_Id(vendorId, userId)) {
            throw new ConflictException("User is already assigned to this vendor");
        }
    }

    public void validateAssignmentExists(Long vendorId, Long userId) {
        boolean exists = vendorUserAssignmentRepository.existsByVendor_IdAndUser_Id(vendorId, userId);
        if (!exists) {
            throw new ResourceNotFoundException("Vendor-user assignment not found");
        }
    }
}