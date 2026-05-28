package com.campusadda.vendorops.security;

import java.util.List;

import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.user.entity.User;
import com.campusadda.vendorops.user.repository.UserRepository;
import com.campusadda.vendorops.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        List<String> roleCodes = userRoleRepository.findByUser_Id(user.getId())
                .stream()
                .map(userRole -> userRole.getRole().getRoleCode())
                .toList();

        return UserPrincipal.create(user, roleCodes);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<String> roleCodes = userRoleRepository.findByUser_Id(user.getId())
                .stream()
                .map(userRole -> userRole.getRole().getRoleCode())
                .toList();

        return UserPrincipal.create(user, roleCodes);
    }
}