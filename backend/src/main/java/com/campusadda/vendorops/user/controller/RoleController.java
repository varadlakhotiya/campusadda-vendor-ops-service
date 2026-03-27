package com.campusadda.vendorops.user.controller;

import java.util.List;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.user.dto.response.RoleResponse;
import com.campusadda.vendorops.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> response = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success("Roles fetched successfully", response));
    }
}