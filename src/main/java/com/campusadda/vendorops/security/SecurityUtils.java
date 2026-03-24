package com.campusadda.vendorops.security;

import java.util.List;

import com.campusadda.vendorops.common.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public Long getCurrentUserId() {
        UserPrincipal principal = getCurrentUserPrincipal();
        return principal.getId();
    }

    public String getCurrentUserEmail() {
        UserPrincipal principal = getCurrentUserPrincipal();
        return principal.getEmail();
    }

    public List<String> getCurrentUserRoles() {
        UserPrincipal principal = getCurrentUserPrincipal();
        return principal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    public boolean hasRole(String roleCode) {
        String requiredAuthority = "ROLE_" + roleCode;
        return getCurrentUserRoles().contains(requiredAuthority);
    }

    public UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedException("No authenticated user found");
        }

        return principal;
    }
}