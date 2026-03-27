package com.campusadda.vendorops.forecast.service.impl;

import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.forecast.dto.request.UpdateRecommendationStatusRequest;
import com.campusadda.vendorops.forecast.dto.response.ReorderRecommendationResponse;
import com.campusadda.vendorops.forecast.entity.ForecastRun;
import com.campusadda.vendorops.forecast.entity.ReorderRecommendation;
import com.campusadda.vendorops.forecast.repository.ForecastRunRepository;
import com.campusadda.vendorops.forecast.repository.ForecastValueRepository;
import com.campusadda.vendorops.forecast.repository.ReorderRecommendationRepository;
import com.campusadda.vendorops.forecast.service.ReorderRecommendationService;
import com.campusadda.vendorops.inventory.entity.InventoryItem;
import com.campusadda.vendorops.inventory.entity.InventoryPolicy;
import com.campusadda.vendorops.inventory.repository.InventoryItemRepository;
import com.campusadda.vendorops.inventory.repository.InventoryPolicyRepository;
import com.campusadda.vendorops.vendor.validator.VendorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReorderRecommendationServiceImpl implements ReorderRecommendationService {

    private final VendorValidator vendorValidator;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryPolicyRepository inventoryPolicyRepository;
    private final ForecastRunRepository forecastRunRepository;
    private final ForecastValueRepository forecastValueRepository;
    private final ReorderRecommendationRepository reorderRecommendationRepository;

    @Override
    public ReorderRecommendationResponse generateForInventoryItem(Long vendorId, Long inventoryItemId) {
        vendorValidator.validateVendorExists(vendorId);

        InventoryItem inventoryItem = inventoryItemRepository.findByIdAndVendor_Id(inventoryItemId, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

        InventoryPolicy policy = inventoryPolicyRepository.findByInventoryItem_Id(inventoryItem.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory policy not found"));

        ForecastRun latestRun = forecastRunRepository.findByVendor_IdOrderByStartedAtDesc(vendorId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Forecast run not found"));

        BigDecimal forecastDemand = forecastValueRepository.findByForecastRun_IdOrderByForecastDateAsc(latestRun.getId())
                .stream()
                .limit(policy.getLeadTimeDays())
                .map(v -> v.getPredictedQuantity() == null ? BigDecimal.ZERO : v.getPredictedQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal safetyStock = policy.getSafetyStockQty() != null ? policy.getSafetyStockQty() : BigDecimal.ZERO;
        BigDecimal reorderPoint = forecastDemand.add(safetyStock);
        BigDecimal currentStock = inventoryItem.getCurrentQuantity();
        BigDecimal suggested = reorderPoint.subtract(currentStock);
        if (suggested.compareTo(BigDecimal.ZERO) < 0) suggested = BigDecimal.ZERO;

        ReorderRecommendation rec = reorderRecommendationRepository
                .findByVendor_IdAndInventoryItem_IdAndRecommendationDate(vendorId, inventoryItemId, LocalDate.now())
                .orElseGet(ReorderRecommendation::new);

        rec.setVendor(inventoryItem.getVendor());
        rec.setInventoryItem(inventoryItem);
        rec.setForecastRun(latestRun);
        rec.setRecommendationDate(LocalDate.now());
        rec.setCurrentStockQty(currentStock);
        rec.setLeadTimeDays(policy.getLeadTimeDays());
        rec.setForecastDemandQty(forecastDemand);
        rec.setSafetyStockQty(safetyStock);
        rec.setReorderPointQty(reorderPoint);
        rec.setSuggestedReorderQty(suggested);
        rec.setRecommendationStatus("OPEN");
        rec.setExplanation("Reorder point = forecast demand during lead time + safety stock");

        return map(reorderRecommendationRepository.save(rec));
    }

    @Override
    public List<ReorderRecommendationResponse> generateForVendor(Long vendorId) {
        vendorValidator.validateVendorExists(vendorId);
        return inventoryItemRepository.findByVendor_IdOrderByItemNameAsc(vendorId)
                .stream()
                .map(item -> generateForInventoryItem(vendorId, item.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReorderRecommendationResponse> getRecommendations(Long vendorId) {
        return reorderRecommendationRepository.findByVendor_IdOrderByRecommendationDateDesc(vendorId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public ReorderRecommendationResponse updateStatus(Long vendorId, Long recommendationId, UpdateRecommendationStatusRequest request) {
        ReorderRecommendation rec = reorderRecommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found"));

        if (!rec.getVendor().getId().equals(vendorId)) {
            throw new ResourceNotFoundException("Recommendation not found");
        }

        rec.setRecommendationStatus(request.getRecommendationStatus());
        return map(reorderRecommendationRepository.save(rec));
    }

    private ReorderRecommendationResponse map(ReorderRecommendation rec) {
        return ReorderRecommendationResponse.builder()
                .id(rec.getId())
                .vendorId(rec.getVendor().getId())
                .inventoryItemId(rec.getInventoryItem().getId())
                .forecastRunId(rec.getForecastRun() != null ? rec.getForecastRun().getId() : null)
                .recommendationDate(rec.getRecommendationDate())
                .currentStockQty(rec.getCurrentStockQty())
                .leadTimeDays(rec.getLeadTimeDays())
                .forecastDemandQty(rec.getForecastDemandQty())
                .safetyStockQty(rec.getSafetyStockQty())
                .reorderPointQty(rec.getReorderPointQty())
                .suggestedReorderQty(rec.getSuggestedReorderQty())
                .recommendationStatus(rec.getRecommendationStatus())
                .explanation(rec.getExplanation())
                .build();
    }
}