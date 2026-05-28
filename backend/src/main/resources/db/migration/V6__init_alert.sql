CREATE TABLE alerts (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    vendor_id BIGINT UNSIGNED NOT NULL,
    alert_type VARCHAR(40) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    entity_type VARCHAR(40) NOT NULL,
    entity_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    metadata_json JSON NULL,
    triggered_at DATETIME(3) NOT NULL,
    acknowledged_at DATETIME(3) NULL,
    resolved_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_alerts_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id)
);

CREATE INDEX idx_alerts_vendor_status ON alerts(vendor_id, status);
CREATE INDEX idx_alerts_vendor_type ON alerts(vendor_id, alert_type);
CREATE INDEX idx_alerts_entity ON alerts(entity_type, entity_id);