package com.campusadda.vendorops.auth.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.campusadda.vendorops.auth.dto.request.ChangePasswordRequest;
import com.campusadda.vendorops.auth.dto.request.CustomerSignupRequest;
import com.campusadda.vendorops.auth.dto.request.LoginRequest;
import com.campusadda.vendorops.auth.dto.request.RefreshTokenRequest;
import com.campusadda.vendorops.auth.dto.response.AuthResponse;
import com.campusadda.vendorops.auth.dto.response.CurrentUserResponse;
import com.campusadda.vendorops.auth.entity.RefreshToken;
import com.campusadda.vendorops.auth.repository.RefreshTokenRepository;
import com.campusadda.vendorops.auth.service.AuthService;
import com.campusadda.vendorops.auth.validator.AuthValidator;
import com.campusadda.vendorops.common.exception.ConflictException;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.security.JwtTokenProvider;
import com.campusadda.vendorops.security.SecurityUtils;
import com.campusadda.vendorops.security.UserPrincipal;
import com.campusadda.vendorops.user.entity.Role;
import com.campusadda.vendorops.user.entity.User;
import com.campusadda.vendorops.user.entity.UserRole;
import com.campusadda.vendorops.user.repository.RoleRepository;
import com.campusadda.vendorops.user.repository.UserRepository;
import com.campusadda.vendorops.user.repository.UserRoleRepository;
import com.campusadda.vendorops.vendor.entity.VendorUserAssignment;
import com.campusadda.vendorops.vendor.repository.VendorUserAssignmentRepository;
import com.campusadda.vendorops.order.entity.Order;
import com.campusadda.vendorops.order.repository.OrderRepository;
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

    private static final String CUSTOMER_ROLE_CODE = "CUSTOMER";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final VendorUserAssignmentRepository vendorUserAssignmentRepository;
    private final OrderRepository orderRepository;
    private final AuthValidator authValidator;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse signupCustomer(CustomerSignupRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        String normalizedPhone = normalizePhone(request.getPhone());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email is already registered");
        }

        if (userRepository.existsByPhone(normalizedPhone)) {
            throw new ConflictException("Phone number is already registered");
        }

        Role customerRole = roleRepository.findByRoleCode(CUSTOMER_ROLE_CODE)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + CUSTOMER_ROLE_CODE));

        User user = new User();
        user.setFullName(safeTrim(request.getFullName()));
        user.setEmail(normalizedEmail);
        user.setPhone(normalizedPhone);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE");
        user.setLastLoginAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        if (!userRoleRepository.existsByUser_IdAndRole_Id(savedUser.getId(), customerRole.getId())) {
            UserRole userRole = new UserRole();
            userRole.setUser(savedUser);
            userRole.setRole(customerRole);
            userRoleRepository.save(userRole);
        }

        attachHistoricalGuestOrdersByPhone(savedUser);
        return issueTokensForUser(savedUser, true);
    }

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
        attachHistoricalGuestOrdersByPhone(user);
        return issueTokensForUser(user, true);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        authValidator.validateRefreshTokenUsable(refreshToken);

        User user = refreshToken.getUser();
        authValidator.validateUserActive(user);

        List<String> roleCodes = resolveRoleCodes(user.getId());
        UserPrincipal principal = UserPrincipal.create(user, roleCodes);
        String accessToken = jwtTokenProvider.generateAccessToken(principal);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(3600L)
                .user(buildUserInfo(user, roleCodes))
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

        List<String> roleCodes = resolveRoleCodes(user.getId());
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
    }

    private AuthResponse issueTokensForUser(User user, boolean updateLastLogin) {
        List<String> roleCodes = resolveRoleCodes(user.getId());
        UserPrincipal principal = UserPrincipal.create(user, roleCodes);

        String accessToken = jwtTokenProvider.generateAccessToken(principal);
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

        if (updateLastLogin) {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        }

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .expiresIn(3600L)
                .user(buildUserInfo(user, roleCodes))
                .build();
    }

    private AuthResponse.UserInfo buildUserInfo(User user, List<String> roleCodes) {
        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(roleCodes)
                .build();
    }

    private List<String> resolveRoleCodes(Long userId) {
        return userRoleRepository.findByUser_Id(userId)
                .stream()
                .map(userRole -> userRole.getRole().getRoleCode())
                .toList();
    }

    private void attachHistoricalGuestOrdersByPhone(User user) {
        Set<String> candidatePhones = new LinkedHashSet<>();

        String rawPhone = safeTrim(user.getPhone());
        if (rawPhone != null && !rawPhone.isBlank()) {
            candidatePhones.add(rawPhone);
            String normalized = normalizePhone(rawPhone);
            if (!normalized.isBlank()) {
                candidatePhones.add(normalized);
            }
        }

        if (candidatePhones.isEmpty()) {
            return;
        }

        boolean updated = false;
        for (String candidatePhone : candidatePhones) {
            List<Order> historicalOrders = orderRepository
                    .findByCustomerPhoneAndExternalCustomerIdIsNullOrderByPlacedAtDesc(candidatePhone);

            for (Order order : historicalOrders) {
                order.setExternalCustomerId(String.valueOf(user.getId()));
                updated = true;
            }

            if (!historicalOrders.isEmpty()) {
                orderRepository.saveAll(historicalOrders);
            }
        }

        if (updated) {
            userRepository.save(user);
        }
    }

    private String normalizeEmail(String value) {
        return safeTrim(value).toLowerCase();
    }

    private String normalizePhone(String value) {
        String trimmed = safeTrim(value);
        return trimmed == null ? "" : trimmed.replaceAll("[^0-9]", "");
    }

    private String safeTrim(String value) {
        return value == null ? null : value.trim();
    }
}