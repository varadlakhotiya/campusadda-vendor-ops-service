package com.campusadda.vendorops.forecast.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.forecast.dto.request.UpdateRecommendationStatusRequest;
import com.campusadda.vendorops.forecast.dto.response.ReorderRecommendationResponse;
import com.campusadda.vendorops.forecast.service.ReorderRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}")
@RequiredArgsConstructor
public class ReorderRecommendationController {

    private final ReorderRecommendationService reorderRecommendationService;

    @PostMapping("/inventory-items/{inventoryItemId}/reorder-recommendations/generate")
    public ResponseEntity<ApiResponse<ReorderRecommendationResponse>> generateOne(
            @PathVariable Long vendorId,
            @PathVariable Long inventoryItemId) {
        return ResponseEntity.ok(ApiResponse.success("Reorder recommendation generated",
                reorderRecommendationService.generateForInventoryItem(vendorId, inventoryItemId)));
    }

    @PostMapping("/reorder-recommendations/generate")
    public ResponseEntity<ApiResponse<List<ReorderRecommendationResponse>>> generateAll(
            @PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success("Reorder recommendations generated",
                reorderRecommendationService.generateForVendor(vendorId)));
    }

    @GetMapping("/reorder-recommendations")
    public ResponseEntity<ApiResponse<List<ReorderRecommendationResponse>>> list(@PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success("Reorder recommendations fetched successfully",
                reorderRecommendationService.getRecommendations(vendorId)));
    }

    @PatchMapping("/reorder-recommendations/{recommendationId}/status")
    public ResponseEntity<ApiResponse<ReorderRecommendationResponse>> updateStatus(
            @PathVariable Long vendorId,
            @PathVariable Long recommendationId,
            @RequestBody UpdateRecommendationStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Recommendation status updated",
                reorderRecommendationService.updateStatus(vendorId, recommendationId, request)));
    }
}