package com.campusadda.vendorops.forecast.service;

import com.campusadda.vendorops.forecast.dto.request.UpdateRecommendationStatusRequest;
import com.campusadda.vendorops.forecast.dto.response.ReorderRecommendationResponse;

import java.util.List;

public interface ReorderRecommendationService {
    ReorderRecommendationResponse generateForInventoryItem(Long vendorId, Long inventoryItemId);
    List<ReorderRecommendationResponse> generateForVendor(Long vendorId);
    List<ReorderRecommendationResponse> getRecommendations(Long vendorId);
    ReorderRecommendationResponse updateStatus(Long vendorId, Long recommendationId, UpdateRecommendationStatusRequest request);
}