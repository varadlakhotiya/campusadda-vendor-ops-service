CREATE TABLE forecast_runs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    vendor_id BIGINT UNSIGNED NOT NULL,
    menu_item_id BIGINT UNSIGNED NOT NULL,
    model_name VARCHAR(50) NOT NULL,
    model_version VARCHAR(50) NOT NULL,
    horizon_days INT UNSIGNED NOT NULL,
    training_start_date DATE NOT NULL,
    training_end_date DATE NOT NULL,
    feature_set_version VARCHAR(50) NULL,
    mlflow_run_id VARCHAR(100) NULL,
    metrics_json JSON NULL,
    status VARCHAR(32) NOT NULL,
    started_at DATETIME(3) NOT NULL,
    completed_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_forecast_runs_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    CONSTRAINT fk_forecast_runs_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

CREATE TABLE forecast_values (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    forecast_run_id BIGINT UNSIGNED NOT NULL,
    forecast_date DATE NOT NULL,
    predicted_quantity DECIMAL(14,3) NOT NULL,
    lower_bound_qty DECIMAL(14,3) NULL,
    upper_bound_qty DECIMAL(14,3) NULL,
    confidence_level_pct DECIMAL(5,2) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_forecast_values_run_date UNIQUE (forecast_run_id, forecast_date),
    CONSTRAINT fk_forecast_values_run FOREIGN KEY (forecast_run_id) REFERENCES forecast_runs(id)
);

CREATE TABLE reorder_recommendations (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    vendor_id BIGINT UNSIGNED NOT NULL,
    inventory_item_id BIGINT UNSIGNED NOT NULL,
    forecast_run_id BIGINT UNSIGNED NULL,
    recommendation_date DATE NOT NULL,
    current_stock_qty DECIMAL(14,3) NOT NULL,
    lead_time_days INT UNSIGNED NOT NULL,
    forecast_demand_qty DECIMAL(14,3) NOT NULL,
    safety_stock_qty DECIMAL(14,3) NOT NULL,
    reorder_point_qty DECIMAL(14,3) NOT NULL,
    suggested_reorder_qty DECIMAL(14,3) NOT NULL,
    recommendation_status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    explanation TEXT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_reorder_recommendation UNIQUE (vendor_id, inventory_item_id, recommendation_date),
    CONSTRAINT fk_reorder_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    CONSTRAINT fk_reorder_inventory_item FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id),
    CONSTRAINT fk_reorder_forecast_run FOREIGN KEY (forecast_run_id) REFERENCES forecast_runs(id)
);

CREATE TABLE anomaly_records (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    vendor_id BIGINT UNSIGNED NOT NULL,
    menu_item_id BIGINT UNSIGNED NULL,
    inventory_item_id BIGINT UNSIGNED NULL,
    anomaly_date DATE NOT NULL,
    anomaly_type VARCHAR(40) NOT NULL,
    observed_value DECIMAL(14,3) NOT NULL,
    expected_value DECIMAL(14,3) NULL,
    deviation_score DECIMAL(14,4) NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'WARNING',
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    details_json JSON NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_anomaly_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    CONSTRAINT fk_anomaly_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id),
    CONSTRAINT fk_anomaly_inventory_item FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id)
);

CREATE INDEX idx_forecast_runs_vendor_item ON forecast_runs(vendor_id, menu_item_id);
CREATE INDEX idx_forecast_runs_status ON forecast_runs(status);
CREATE INDEX idx_forecast_values_date ON forecast_values(forecast_date);
CREATE INDEX idx_reorder_vendor_status ON reorder_recommendations(vendor_id, recommendation_status);
CREATE INDEX idx_anomaly_vendor_date ON anomaly_records(vendor_id, anomaly_date);
CREATE INDEX idx_anomaly_status ON anomaly_records(status);