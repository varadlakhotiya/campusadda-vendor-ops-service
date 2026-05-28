CREATE TABLE calendar_events (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    event_date DATE NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NULL,
    impact_level TINYINT UNSIGNED NOT NULL DEFAULT 1,
    campus_area VARCHAR(100) NULL,
    vendor_id BIGINT UNSIGNED NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_calendar_events_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id)
);

CREATE INDEX idx_calendar_events_date ON calendar_events(event_date);
CREATE INDEX idx_calendar_events_type ON calendar_events(event_type);
CREATE INDEX idx_calendar_events_vendor_date ON calendar_events(vendor_id, event_date);