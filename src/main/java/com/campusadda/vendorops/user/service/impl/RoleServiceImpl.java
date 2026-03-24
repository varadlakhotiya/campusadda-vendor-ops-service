package com.campusadda.vendorops.user.service.impl;

import java.util.List;

import com.campusadda.vendorops.user.dto.request.AssignRoleRequest;
import com.campusadda.vendorops.user.dto.response.RoleResponse;
import com.campusadda.vendorops.user.entity.Role;
import com.campusadda.vendorops.user.entity.User;
import com.campusadda.vendorops.user.entity.UserRole;
import com.campusadda.vendorops.user.mapper.RoleMapper;
import com.campusadda.vendorops.user.repository.UserRoleRepository;
import com.campusadda.vendorops.user.service.RoleService;
import com.campusadda.vendorops.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final com.campusadda.vendorops.user.repository.RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserValidator userValidator;
    private final RoleMapper roleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(roleMapper::toResponse)
                .toList();
    }

    @Override
    public void assignRoleToUser(Long userId, AssignRoleRequest request) {
        User user = userValidator.validateUserExists(userId);
        Role role = userValidator.validateRoleExistsByCode(request.getRoleCode());
        userValidator.validateRoleNotAlreadyAssigned(user.getId(), role.getId());

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);

        userRoleRepository.save(userRole);
    }

    @Override
    public void removeRoleFromUser(Long userId, String roleCode) {
        User user = userValidator.validateUserExists(userId);
        Role role = userValidator.validateRoleExistsByCode(roleCode);

        userRoleRepository.deleteByUser_IdAndRole_Id(user.getId(), role.getId());
    }
}