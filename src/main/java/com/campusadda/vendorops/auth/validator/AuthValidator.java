package com.campusadda.vendorops.auth.validator;

import java.time.LocalDateTime;

import com.campusadda.vendorops.auth.entity.RefreshToken;
import com.campusadda.vendorops.common.exception.BusinessException;
import com.campusadda.vendorops.common.exception.UnauthorizedException;
import com.campusadda.vendorops.user.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AuthValidator {

    public void validateUserActive(User user) {
        if (user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new UnauthorizedException("User account is not active");
        }
    }

    public void validatePassword(String rawPassword, String encodedPassword, PasswordEncoder passwordEncoder) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    public void validateRefreshTokenUsable(RefreshToken refreshToken) {
        if (refreshToken == null) {
            throw new UnauthorizedException("Refresh token is invalid");
        }

        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiryAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token has expired");
        }
    }

    public void validateNewPasswordDifferent(String currentPassword, String newPassword) {
        if (currentPassword != null && currentPassword.equals(newPassword)) {
            throw new BusinessException("New password must be different from current password");
        }
    }
}