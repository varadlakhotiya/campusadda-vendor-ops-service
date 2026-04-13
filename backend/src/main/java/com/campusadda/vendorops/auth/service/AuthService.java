package com.campusadda.vendorops.auth.service;

import com.campusadda.vendorops.auth.dto.request.ChangePasswordRequest;
import com.campusadda.vendorops.auth.dto.request.CustomerSignupRequest;
import com.campusadda.vendorops.auth.dto.request.LoginRequest;
import com.campusadda.vendorops.auth.dto.request.RefreshTokenRequest;
import com.campusadda.vendorops.auth.dto.response.AuthResponse;
import com.campusadda.vendorops.auth.dto.response.CurrentUserResponse;

public interface AuthService {

    AuthResponse signupCustomer(CustomerSignupRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    CurrentUserResponse getCurrentUser();

    void changePassword(ChangePasswordRequest request);
}