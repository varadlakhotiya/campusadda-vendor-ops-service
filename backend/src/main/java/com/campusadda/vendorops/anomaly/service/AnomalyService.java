package com.campusadda.vendorops.anomaly.service;

import com.campusadda.vendorops.anomaly.dto.response.AnomalyResponse;

import java.util.List;

public interface AnomalyService {
    List<AnomalyResponse> scan(Long vendorId);
    List<AnomalyResponse> getAnomalies(Long vendorId);
    AnomalyResponse resolve(Long vendorId, Long anomalyId);
}