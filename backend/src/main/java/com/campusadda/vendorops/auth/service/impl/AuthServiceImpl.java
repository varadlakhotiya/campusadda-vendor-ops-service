package com.campusadda.vendorops.auth.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.campusadda.vendorops.auth.dto.request.ChangePasswordRequest;
import com.campusadda.vendorops.auth.dto.request.LoginRequest;
import com.campusadda.vendorops.auth.dto.request.RefreshTokenRequest;
import com.campusadda.vendorops.auth.dto.response.AuthResponse;
import com.campusadda.vendorops.auth.dto.response.CurrentUserResponse;
import com.campusadda.vendorops.auth.entity.RefreshToken;
import com.campusadda.vendorops.auth.repository.RefreshTokenRepository;
import com.campusadda.vendorops.auth.service.AuthService;
import com.campusadda.vendorops.auth.validator.AuthValidator;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.security.JwtTokenProvider;
import com.campusadda.vendorops.security.SecurityUtils;
import com.campusadda.vendorops.security.UserPrincipal;
import com.campusadda.vendorops.user.entity.User;
import com.campusadda.vendorops.user.repository.UserRepository;
import com.campusadda.vendorops.user.repository.UserRoleRepository;
import com.campusadda.vendorops.vendor.entity.VendorUserAssignment;
import com.campusadda.vendorops.vendor.repository.VendorUserAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final VendorUserAssignmentRepository vendorUserAssignmentRepository;
    private final AuthValidator authValidator;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));

        authValidator.validateUserActive(user);

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setExpiryAt(LocalDateTime.now().plus(
                jwtTokenProvider.getRefreshTokenExpirationMs(),
                ChronoUnit.MILLIS
        ));
        refreshToken.setRevoked(Boolean.FALSE);
        refreshTokenRepository.save(refreshToken);

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        List<String> roleCodes = userRoleRepository.findByUser_Id(user.getId())
                .stream()
                .map(userRole -> userRole.getRole().getRoleCode())
                .toList();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .expiresIn(3600L)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .roles(roleCodes)
                        .build())
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        authValidator.validateRefreshTokenUsable(refreshToken);

        User user = refreshToken.getUser();
        authValidator.validateUserActive(user);

        List<String> roleCodes = userRoleRepository.findByUser_Id(user.getId())
                .stream()
                .map(userRole -> userRole.getRole().getRoleCode())
                .toList();

        UserPrincipal principal = UserPrincipal.create(user, roleCodes);
        String accessToken = jwtTokenProvider.generateAccessToken(principal);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(3600L)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .roles(roleCodes)
                        .build())
                .build();
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(token -> {
                    token.setRevoked(Boolean.TRUE);
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser() {
        Long currentUserId = securityUtils.getCurrentUserId();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        List<String> roleCodes = userRoleRepository.findByUser_Id(user.getId())
                .stream()
                .map(userRole -> userRole.getRole().getRoleCode())
                .toList();

        List<VendorUserAssignment> assignments = vendorUserAssignmentRepository.findByUser_Id(user.getId());

        List<CurrentUserResponse.AssignedVendorInfo> assignedVendorInfos = assignments.stream()
                .map(assignment -> CurrentUserResponse.AssignedVendorInfo.builder()
                        .vendorId(assignment.getVendor().getId())
                        .vendorCode(assignment.getVendor().getVendorCode())
                        .vendorName(assignment.getVendor().getName())
                        .assignmentRole(assignment.getAssignmentRole())
                        .isPrimary(assignment.getIsPrimary())
                        .build())
                .toList();

        return CurrentUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .roles(roleCodes)
                .assignedVendors(assignedVendorInfos)
                .build();
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        authValidator.validatePassword(
                request.getCurrentPassword(),
                user.getPasswordHash(),
                passwordEncoder
        );

        authValidator.validateNewPasswordDifferent(
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Optional future enhancement:
        // revoke all active refresh tokens for this user after password change.
    }
}