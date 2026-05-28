package com.campusadda.vendorops.forecast.entity;

import com.campusadda.vendorops.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "forecast_values")
public class ForecastValue extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "forecast_run_id", nullable = false)
    private ForecastRun forecastRun;

    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate;

    @Column(name = "predicted_quantity", nullable = false, precision = 14, scale = 3)
    private BigDecimal predictedQuantity;

    @Column(name = "lower_bound_qty", precision = 14, scale = 3)
    private BigDecimal lowerBoundQty;

    @Column(name = "upper_bound_qty", precision = 14, scale = 3)
    private BigDecimal upperBoundQty;

    @Column(name = "confidence_level_pct", precision = 5, scale = 2)
    private BigDecimal confidenceLevelPct;
}