package com.campusadda.vendorops.forecast.entity;

import com.campusadda.vendorops.common.entity.AuditableEntity;
import com.campusadda.vendorops.menu.entity.MenuItem;
import com.campusadda.vendorops.vendor.entity.Vendor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "forecast_runs")
public class ForecastRun extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(name = "model_name", nullable = false, length = 50)
    private String modelName;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @Column(name = "horizon_days", nullable = false)
    private Integer horizonDays;

    @Column(name = "training_start_date", nullable = false)
    private LocalDate trainingStartDate;

    @Column(name = "training_end_date", nullable = false)
    private LocalDate trainingEndDate;

    @Column(name = "feature_set_version", length = 50)
    private String featureSetVersion;

    @Column(name = "mlflow_run_id", length = 100)
    private String mlflowRunId;

    @Column(name = "metrics_json", columnDefinition = "json")
    private String metricsJson;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}