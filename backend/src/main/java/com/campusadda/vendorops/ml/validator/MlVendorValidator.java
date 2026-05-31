package com.campusadda.vendorops.ml.validator;

import com.campusadda.vendorops.common.exception.ConflictException;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.ml.repository.MlUserRepository;
import com.campusadda.vendorops.ml.repository.MlVendorRepository;
import com.campusadda.vendorops.ml.repository.MlVendorUserAssignmentRepository;
import com.campusadda.vendorops.user.entity.User;
import com.campusadda.vendorops.vendor.entity.Vendor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MlVendorValidator {

    private final MlVendorRepository vendorRepository;
    private final MlVendorUserAssignmentRepository vendorUserAssignmentRepository;
    private final MlUserRepository userRepository;

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