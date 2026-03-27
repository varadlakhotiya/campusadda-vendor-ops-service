package com.campusadda.vendorops.forecast.repository;

import com.campusadda.vendorops.forecast.entity.ReorderRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReorderRecommendationRepository extends JpaRepository<ReorderRecommendation, Long> {
    List<ReorderRecommendation> findByVendor_IdOrderByRecommendationDateDesc(Long vendorId);
    Optional<ReorderRecommendation> findByVendor_IdAndInventoryItem_IdAndRecommendationDate(Long vendorId, Long inventoryItemId, LocalDate recommendationDate);
}