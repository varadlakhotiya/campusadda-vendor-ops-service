package com.campusadda.vendorops.forecast.service.impl;

import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.forecast.client.request.MlGenerateReorderRecommendationsRequest;
import com.campusadda.vendorops.forecast.dto.request.UpdateRecommendationStatusRequest;
import com.campusadda.vendorops.forecast.dto.response.ReorderRecommendationResponse;
import com.campusadda.vendorops.forecast.entity.ForecastRun;
import com.campusadda.vendorops.forecast.entity.ReorderRecommendation;
import com.campusadda.vendorops.forecast.repository.ForecastRunRepository;
import com.campusadda.vendorops.forecast.repository.ForecastValueRepository;
import com.campusadda.vendorops.forecast.repository.ReorderRecommendationRepository;
import com.campusadda.vendorops.forecast.service.MlClientService;
import com.campusadda.vendorops.forecast.service.ReorderRecommendationService;
import com.campusadda.vendorops.inventory.entity.InventoryItem;
import com.campusadda.vendorops.inventory.entity.InventoryPolicy;
import com.campusadda.vendorops.inventory.repository.InventoryItemRepository;
import com.campusadda.vendorops.inventory.repository.InventoryPolicyRepository;
import com.campusadda.vendorops.security.VendorAccessService;
import com.campusadda.vendorops.vendor.validator.VendorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReorderRecommendationServiceImpl implements ReorderRecommendationService {

    private final VendorValidator vendorValidator;
    private final VendorAccessService vendorAccessService;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryPolicyRepository inventoryPolicyRepository;
    private final ForecastRunRepository forecastRunRepository;
    private final ForecastValueRepository forecastValueRepository;
    private final ReorderRecommendationRepository reorderRecommendationRepository;
    private final MlClientService mlClientService;

    @Override
    public ReorderRecommendationResponse generateForInventoryItem(Long vendorId, Long inventoryItemId) {
        vendorAccessService.validateVendorAccess(vendorId);
        vendorValidator.validateVendorExists(vendorId);

        InventoryItem inventoryItem = inventoryItemRepository.findByIdAndVendor_Id(inventoryItemId, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

        InventoryPolicy policy = inventoryPolicyRepository.findByInventoryItem_Id(inventoryItem.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory policy not found"));

        ForecastRun latestRun = forecastRunRepository.findByVendor_IdOrderByStartedAtDesc(vendorId)
                .stream()
                .filter(run -> "SUCCESS".equalsIgnoreCase(run.getStatus()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Forecast run not found"));

        BigDecimal forecastDemand = forecastValueRepository.findByForecastRun_IdOrderByForecastDateAsc(latestRun.getId())
                .stream()
                .limit(Math.max(policy.getLeadTimeDays() != null ? policy.getLeadTimeDays() : 1, 1))
                .map(v -> v.getUpperBoundQty() != null ? v.getUpperBoundQty()
                        : (v.getPredictedQuantity() != null ? v.getPredictedQuantity() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal safetyStock = defaultZero(policy.getSafetyStockQty());
        BigDecimal reorderPoint = max(defaultZero(policy.getReorderPointQty()), forecastDemand.add(safetyStock));
        BigDecimal currentStock = defaultZero(inventoryItem.getCurrentQuantity());
        BigDecimal reserved = defaultZero(inventoryItem.getReservedQuantity());
        BigDecimal available = currentStock.subtract(reserved);
        BigDecimal target = reorderPoint.multiply(new BigDecimal("1.10"));
        BigDecimal suggested = target.subtract(available);

        if (suggested.compareTo(BigDecimal.ZERO) < 0) {
            suggested = BigDecimal.ZERO;
        }

        BigDecimal minReorder = defaultZero(policy.getMinReorderQty());
        BigDecimal maxReorder = defaultZero(policy.getMaxReorderQty());

        if (suggested.compareTo(BigDecimal.ZERO) > 0 && minReorder.compareTo(BigDecimal.ZERO) > 0 && suggested.compareTo(minReorder) < 0) {
            suggested = minReorder;
        }
        if (maxReorder.compareTo(BigDecimal.ZERO) > 0 && suggested.compareTo(maxReorder) > 0) {
            suggested = maxReorder;
        }

        suggested = suggested.setScale(3, RoundingMode.HALF_UP);

        ReorderRecommendation rec = reorderRecommendationRepository
                .findByVendor_IdAndInventoryItem_IdAndRecommendationDate(vendorId, inventoryItemId, LocalDate.now())
                .orElseGet(ReorderRecommendation::new);

        rec.setVendor(inventoryItem.getVendor());
        rec.setInventoryItem(inventoryItem);
        rec.setForecastRun(latestRun);
        rec.setRecommendationDate(LocalDate.now());
        rec.setCurrentStockQty(available.max(BigDecimal.ZERO));
        rec.setLeadTimeDays(policy.getLeadTimeDays());
        rec.setForecastDemandQty(forecastDemand);
        rec.setSafetyStockQty(safetyStock);
        rec.setReorderPointQty(reorderPoint);
        rec.setSuggestedReorderQty(suggested);
        rec.setRecommendationStatus(suggested.compareTo(BigDecimal.ZERO) > 0 ? "OPEN" : "SAFE");
        rec.setExplanation("Fallback reorder logic = max(reorder point, upper-band lead-time demand + safety stock) with 10% presentation buffer");

        return map(reorderRecommendationRepository.save(rec));
    }

    @Override
    public List<ReorderRecommendationResponse> generateForVendor(Long vendorId) {
        vendorAccessService.validateVendorAccess(vendorId);
        vendorValidator.validateVendorExists(vendorId);

        mlClientService.generateReorderRecommendations(
                MlGenerateReorderRecommendationsRequest.builder()
                        .configPath(null)
                        .build()
        );

        return getRecommendations(vendorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReorderRecommendationResponse> getRecommendations(Long vendorId) {
        vendorAccessService.validateVendorAccess(vendorId);
        return reorderRecommendationRepository.findByVendor_IdOrderByRecommendationDateDesc(vendorId)
                .stream()
                .sorted(Comparator
                        .comparing(ReorderRecommendation::getRecommendationDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(r -> defaultZero(r.getSuggestedReorderQty()), Comparator.reverseOrder()))
                .map(this::map)
                .toList();
    }

    @Override
    public ReorderRecommendationResponse updateStatus(Long vendorId, Long recommendationId, UpdateRecommendationStatusRequest request) {
        vendorAccessService.validateVendorAccess(vendorId);
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

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal max(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) >= 0 ? left : right;
    }
}
