package com.campusadda.vendorops.user.validator;

import com.campusadda.vendorops.common.exception.ConflictException;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.user.entity.Role;
import com.campusadda.vendorops.user.entity.User;
import com.campusadda.vendorops.user.repository.RoleRepository;
import com.campusadda.vendorops.user.repository.UserRepository;
import com.campusadda.vendorops.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public void validateEmailUnique(String email) {
        if (email != null && userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }
    }

    public void validatePhoneUnique(String phone) {
        if (phone != null && !phone.isBlank() && userRepository.existsByPhone(phone)) {
            throw new ConflictException("Phone already exists");
        }
    }

    public void validateEmailUniqueForUpdate(Long userId, String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        userRepository.findByEmail(email)
                .filter(user -> !user.getId().equals(userId))
                .ifPresent(user -> {
                    throw new ConflictException("Email already exists");
                });
    }

    public void validatePhoneUniqueForUpdate(Long userId, String phone) {
        if (phone == null || phone.isBlank()) {
            return;
        }

        userRepository.findAll().stream()
                .filter(user -> phone.equals(user.getPhone()) && !user.getId().equals(userId))
                .findFirst()
                .ifPresent(user -> {
                    throw new ConflictException("Phone already exists");
                });
    }

    public User validateUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public Role validateRoleExistsByCode(String roleCode) {
        return roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with code: " + roleCode));
    }

    public void validateRoleNotAlreadyAssigned(Long userId, Long roleId) {
        if (userRoleRepository.existsByUser_IdAndRole_Id(userId, roleId)) {
            throw new ConflictException("Role is already assigned to the user");
        }
    }
}