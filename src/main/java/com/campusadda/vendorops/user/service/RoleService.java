package com.campusadda.vendorops.user.service;

import java.util.List;

import com.campusadda.vendorops.user.dto.request.AssignRoleRequest;
import com.campusadda.vendorops.user.dto.response.RoleResponse;

public interface RoleService {

    List<RoleResponse> getAllRoles();

    void assignRoleToUser(Long userId, AssignRoleRequest request);

    void removeRoleFromUser(Long userId, String roleCode);
}