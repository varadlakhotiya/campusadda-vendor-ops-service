package com.campusadda.vendorops.auth.controller;

import com.campusadda.vendorops.auth.dto.request.ChangePasswordRequest;
import com.campusadda.vendorops.auth.dto.request.LoginRequest;
import com.campusadda.vendorops.auth.dto.request.RefreshTokenRequest;
import com.campusadda.vendorops.auth.dto.response.AuthResponse;
import com.campusadda.vendorops.auth.dto.response.CurrentUserResponse;
import com.campusadda.vendorops.auth.service.AuthService;
import com.campusadda.vendorops.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {

        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> getCurrentUser() {
        CurrentUserResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Current user fetched successfully", response));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Password changed successfully", null));
    }
}