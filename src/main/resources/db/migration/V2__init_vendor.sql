CREATE TABLE vendors (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    vendor_code VARCHAR(40) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    contact_name VARCHAR(120) NULL,
    contact_phone VARCHAR(20) NULL,
    contact_email VARCHAR(150) NULL,
    location_label VARCHAR(150) NULL,
    campus_area VARCHAR(100) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    source_system VARCHAR(40) NOT NULL DEFAULT 'VENDOR_OPS',
    external_vendor_id VARCHAR(64) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_vendors_vendor_code UNIQUE (vendor_code)
);

CREATE TABLE vendor_user_assignments (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    vendor_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    assignment_role VARCHAR(32) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_vendor_user_assignments_vendor_user UNIQUE (vendor_id, user_id),
    CONSTRAINT fk_vendor_user_assignments_vendor
        FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    CONSTRAINT fk_vendor_user_assignments_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_vendors_name ON vendors(name);
CREATE INDEX idx_vendors_status ON vendors(status);
CREATE INDEX idx_vendors_source_external ON vendors(source_system, external_vendor_id);
CREATE INDEX idx_vendor_user_assignments_vendor_id ON vendor_user_assignments(vendor_id);
CREATE INDEX idx_vendor_user_assignments_user_id ON vendor_user_assignments(user_id);
CREATE INDEX idx_vendor_user_assignments_status ON vendor_user_assignments(status);