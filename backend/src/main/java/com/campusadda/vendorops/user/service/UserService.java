package com.campusadda.vendorops.user.service;

import java.util.List;

import com.campusadda.vendorops.user.dto.request.CreateUserRequest;
import com.campusadda.vendorops.user.dto.request.UpdateUserRequest;
import com.campusadda.vendorops.user.dto.request.UpdateUserStatusRequest;
import com.campusadda.vendorops.user.dto.response.UserDetailResponse;
import com.campusadda.vendorops.user.dto.response.UserResponse;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(Long userId, UpdateUserRequest request);

    UserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request);

    UserDetailResponse getUserById(Long userId);

    List<UserResponse> getAllUsers();
}