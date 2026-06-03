package com.campusadda.vendorops.anomaly.service.impl;

import com.campusadda.vendorops.anomaly.dto.response.AnomalyResponse;
import com.campusadda.vendorops.anomaly.entity.AnomalyRecord;
import com.campusadda.vendorops.ml.repository.MlAnomalyRecordRepository;
import com.campusadda.vendorops.anomaly.service.AnomalyService;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.forecast.client.request.MlGenerateAnomaliesRequest;
import com.campusadda.vendorops.forecast.service.MlClientService;
import com.campusadda.vendorops.security.VendorAccessService;
import com.campusadda.vendorops.vendor.validator.VendorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnomalyServiceImpl implements AnomalyService {

    private final VendorValidator vendorValidator;
    private final VendorAccessService vendorAccessService;
    private final MlAnomalyRecordRepository anomalyRecordRepository;
    private final MlClientService mlClientService;

    @Override
public List<AnomalyResponse> scan(Long vendorId) {
    vendorAccessService.validateVendorAccess(vendorId);
    vendorValidator.validateVendorExists(vendorId);

    mlClientService.generateAnomalies(
        MlGenerateAnomaliesRequest.builder()
            .configPath("config/ml_training_config.json")
            .vendorId(vendorId)
            .build()
    );

    return getAnomalies(vendorId);
}

    @Override
    @Transactional(readOnly = true, transactionManager = "mlTransactionManager")
    public List<AnomalyResponse> getAnomalies(Long vendorId) {
        vendorAccessService.validateVendorAccess(vendorId);
        LocalDate cutoff = LocalDate.now().minusDays(90);
return anomalyRecordRepository
    .findByVendor_IdAndStatusAndAnomalyDateGreaterThanEqualOrderByAnomalyDateDesc(
        vendorId,
        "OPEN",
        cutoff
    )
    .stream()
    .map(this::map)
    .toList();
    }

    @Override
    @Transactional(transactionManager = "mlTransactionManager")
    public AnomalyResponse resolve(Long vendorId, Long anomalyId) {
        vendorAccessService.validateVendorAccess(vendorId);
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
