package com.campusadda.vendorops.user.controller;

import java.util.List;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.user.dto.request.AssignRoleRequest;
import com.campusadda.vendorops.user.dto.request.CreateUserRequest;
import com.campusadda.vendorops.user.dto.request.UpdateUserRequest;
import com.campusadda.vendorops.user.dto.request.UpdateUserStatusRequest;
import com.campusadda.vendorops.user.dto.response.UserDetailResponse;
import com.campusadda.vendorops.user.dto.response.UserResponse;
import com.campusadda.vendorops.user.service.RoleService;
import com.campusadda.vendorops.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", response));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserById(
            @PathVariable Long userId) {

        UserDetailResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", response));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request) {

        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {

        UserResponse response = userService.updateUserStatus(userId, request);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", response));
    }

    @PostMapping("/{userId}/roles")
    public ResponseEntity<ApiResponse<Void>> assignRoleToUser(
            @PathVariable Long userId,
            @Valid @RequestBody AssignRoleRequest request) {

        roleService.assignRoleToUser(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role assigned successfully", null));
    }

    @DeleteMapping("/{userId}/roles/{roleCode}")
    public ResponseEntity<ApiResponse<Void>> removeRoleFromUser(
            @PathVariable Long userId,
            @PathVariable String roleCode) {

        roleService.removeRoleFromUser(userId, roleCode);
        return ResponseEntity.ok(ApiResponse.success("Role removed successfully", null));
    }
}