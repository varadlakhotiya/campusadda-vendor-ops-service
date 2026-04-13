package com.campusadda.vendorops.portal.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.portal.dto.response.PortalContextResponse;
import com.campusadda.vendorops.portal.service.PortalContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/portal")
@RequiredArgsConstructor
public class PortalController {

    private final PortalContextService portalContextService;

    @GetMapping("/context")
    public ResponseEntity<ApiResponse<PortalContextResponse>> getContext() {
        return ResponseEntity.ok(ApiResponse.success(
                "Portal context fetched successfully",
                portalContextService.getCurrentContext()
        ));
    }
}