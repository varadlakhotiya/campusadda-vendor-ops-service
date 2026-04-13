package com.campusadda.vendorops.anomaly.service.impl;

import com.campusadda.vendorops.anomaly.dto.response.AnomalyResponse;
import com.campusadda.vendorops.anomaly.entity.AnomalyRecord;
import com.campusadda.vendorops.anomaly.repository.AnomalyRecordRepository;
import com.campusadda.vendorops.anomaly.service.AnomalyService;
import com.campusadda.vendorops.analytics.repository.DailyItemSalesRepository;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.vendor.validator.VendorValidator;
import com.campusadda.vendorops.security.VendorAccessService; // ✅ ADD
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AnomalyServiceImpl implements AnomalyService {

    private final VendorValidator vendorValidator;
    private final DailyItemSalesRepository dailyItemSalesRepository;
    private final AnomalyRecordRepository anomalyRecordRepository;
    private final VendorAccessService vendorAccessService; // ✅ ADD

    @Override
    public List<AnomalyResponse> scan(Long vendorId) {
        vendorAccessService.validateVendorAccess(vendorId); // 🔐 ADD
        vendorValidator.validateVendorExists(vendorId);

        LocalDate from = LocalDate.now().minusDays(30);
        LocalDate to = LocalDate.now();

        var rows = dailyItemSalesRepository.findByVendor_IdAndSalesDateBetweenOrderBySalesDateAsc(vendorId, from, to);

        List<AnomalyResponse> responses = new ArrayList<>();

        rows.stream()
                .collect(java.util.stream.Collectors.groupingBy(r -> r.getMenuItem().getId()))
                .forEach((menuItemId, itemRows) -> {
                    if (itemRows.size() < 7) return;

                    BigDecimal avg = itemRows.stream()
                            .map(r -> BigDecimal.valueOf(r.getQuantitySold()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(itemRows.size()), 2, RoundingMode.HALF_UP);

                    itemRows.forEach(r -> {
                        BigDecimal observed = BigDecimal.valueOf(r.getQuantitySold());
                        BigDecimal deviation = observed.subtract(avg).abs();

                        if (avg.compareTo(BigDecimal.ZERO) > 0 && deviation.compareTo(avg.multiply(new BigDecimal("1.5"))) > 0) {
                            AnomalyRecord record = new AnomalyRecord();
                            record.setVendor(r.getVendor());
                            record.setMenuItem(r.getMenuItem());
                            record.setAnomalyDate(r.getSalesDate());
                            record.setAnomalyType("DEMAND_SPIKE");
                            record.setObservedValue(observed);
                            record.setExpectedValue(avg);
                            record.setDeviationScore(deviation);
                            record.setSeverity("WARNING");
                            record.setStatus("OPEN");
                            record.setDetailsJson("{\"rule\":\"deviation_gt_1.5x_avg\"}");

                            responses.add(map(anomalyRecordRepository.save(record)));
                        }
                    });
                });

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnomalyResponse> getAnomalies(Long vendorId) {
        vendorAccessService.validateVendorAccess(vendorId); // 🔐 ADD

        return anomalyRecordRepository.findByVendor_IdOrderByAnomalyDateDesc(vendorId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public AnomalyResponse resolve(Long vendorId, Long anomalyId) {
        vendorAccessService.validateVendorAccess(vendorId); // 🔐 ADD

        AnomalyRecord record = anomalyRecordRepository.findById(anomalyId)
                .orElseThrow(() -> new ResourceNotFoundException("Anomaly not found"));

        if (!record.getVendor().getId().equals(vendorId)) {
            throw new ResourceNotFoundException("Anomaly not found");
        }

        record.setStatus("RESOLVED");
        return map(anomalyRecordRepository.save(record));
    }

    private AnomalyResponse map(AnomalyRecord record) {
        return AnomalyResponse.builder()
                .id(record.getId())
                .vendorId(record.getVendor().getId())
                .menuItemId(record.getMenuItem() != null ? record.getMenuItem().getId() : null)
                .menuItemName(record.getMenuItem() != null ? record.getMenuItem().getItemName() : null)
                .anomalyDate(record.getAnomalyDate())
                .anomalyType(record.getAnomalyType())
                .observedValue(record.getObservedValue())
                .expectedValue(record.getExpectedValue())
                .deviationScore(record.getDeviationScore())
                .severity(record.getSeverity())
                .status(record.getStatus())
                .detailsJson(record.getDetailsJson())
                .build();
    }
}