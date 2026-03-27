package com.campusadda.vendorops.config;

import com.campusadda.vendorops.user.entity.Role;
import com.campusadda.vendorops.user.entity.User;
import com.campusadda.vendorops.user.entity.UserRole;
import com.campusadda.vendorops.user.repository.RoleRepository;
import com.campusadda.vendorops.user.repository.UserRepository;
import com.campusadda.vendorops.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.default-admin.enabled:true}")
    private boolean defaultAdminEnabled;

    @Value("${app.seed.default-admin.full-name:Super Admin}")
    private String defaultAdminFullName;

    @Value("${app.seed.default-admin.email:admin@campusadda.com}")
    private String defaultAdminEmail;

    @Value("${app.seed.default-admin.phone:9999999999}")
    private String defaultAdminPhone;

    @Value("${app.seed.default-admin.password:Admin@123}")
    private String defaultAdminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting application seed process");

        Role adminRole = createRoleIfNotExists(
                "ADMIN",
                "Administrator",
                "Full system access"
        );

        Role vendorManagerRole = createRoleIfNotExists(
                "VENDOR_MANAGER",
                "Vendor Manager",
                "Vendor management access"
        );

        Role vendorStaffRole = createRoleIfNotExists(
                "VENDOR_STAFF",
                "Vendor Staff",
                "Vendor staff access"
        );

        if (defaultAdminEnabled) {
            User adminUser = createDefaultAdminIfNotExists();
            assignRoleIfNotAssigned(adminUser, adminRole);
        }

        log.info("Application seed process completed");
    }

    private Role createRoleIfNotExists(String roleCode, String roleName, String description) {
        return roleRepository.findByRoleCode(roleCode)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleCode(roleCode);
                    role.setRoleName(roleName);
                    role.setDescription(description);

                    Role savedRole = roleRepository.save(role);
                    log.info("Seeded role: {}", roleCode);
                    return savedRole;
                });
    }

    private User createDefaultAdminIfNotExists() {
        return userRepository.findByEmail(defaultAdminEmail)
                .orElseGet(() -> {
                    User user = new User();
                    user.setFullName(defaultAdminFullName);
                    user.setEmail(defaultAdminEmail);
                    user.setPhone(defaultAdminPhone);
                    user.setPasswordHash(passwordEncoder.encode(defaultAdminPassword));
                    user.setStatus("ACTIVE");

                    User savedUser = userRepository.save(user);
                    log.info("Seeded default admin user: {}", defaultAdminEmail);
                    return savedUser;
                });
    }

    private void assignRoleIfNotAssigned(User user, Role role) {
        boolean alreadyAssigned = userRoleRepository.existsByUser_IdAndRole_Id(user.getId(), role.getId());

        if (alreadyAssigned) {
            return;
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);

        log.info("Assigned role {} to user {}", role.getRoleCode(), user.getEmail());
    }
}