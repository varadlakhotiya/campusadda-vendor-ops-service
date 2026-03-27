package com.campusadda.vendorops.user.service.impl;

import java.util.List;

import com.campusadda.vendorops.user.dto.request.CreateUserRequest;
import com.campusadda.vendorops.user.dto.request.UpdateUserRequest;
import com.campusadda.vendorops.user.dto.request.UpdateUserStatusRequest;
import com.campusadda.vendorops.user.dto.response.RoleResponse;
import com.campusadda.vendorops.user.dto.response.UserDetailResponse;
import com.campusadda.vendorops.user.dto.response.UserResponse;
import com.campusadda.vendorops.user.entity.User;
import com.campusadda.vendorops.user.mapper.RoleMapper;
import com.campusadda.vendorops.user.mapper.UserMapper;
import com.campusadda.vendorops.user.repository.UserRepository;
import com.campusadda.vendorops.user.repository.UserRoleRepository;
import com.campusadda.vendorops.user.service.UserService;
import com.campusadda.vendorops.user.validator.UserValidator;
import com.campusadda.vendorops.vendor.entity.VendorUserAssignment;
import com.campusadda.vendorops.vendor.repository.VendorUserAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final VendorUserAssignmentRepository vendorUserAssignmentRepository;
    private final UserValidator userValidator;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        userValidator.validateEmailUnique(request.getEmail());
        userValidator.validatePhoneUnique(request.getPhone());

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(request.getStatus());

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userValidator.validateUserExists(userId);

        if (request.getEmail() != null) {
            userValidator.validateEmailUniqueForUpdate(userId, request.getEmail());
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            userValidator.validatePhoneUniqueForUpdate(userId, request.getPhone());
            user.setPhone(request.getPhone());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    public UserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        User user = userValidator.validateUserExists(userId);
        user.setStatus(request.getStatus());

        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserById(Long userId) {
        User user = userValidator.validateUserExists(userId);

        List<RoleResponse> roles = userRoleRepository.findByUser_Id(userId)
                .stream()
                .map(userRole -> roleMapper.toResponse(userRole.getRole()))
                .toList();

        List<VendorUserAssignment> assignments = vendorUserAssignmentRepository.findByUser_Id(userId);

        return userMapper.toDetailResponse(user, roles, assignments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }
}