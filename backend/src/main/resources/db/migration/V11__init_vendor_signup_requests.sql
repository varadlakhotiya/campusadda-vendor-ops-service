CREATE TABLE vendor_signup_requests (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    restaurant_name VARCHAR(150) NOT NULL,
    contact_person_name VARCHAR(150) NOT NULL,
    contact_email VARCHAR(150) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    campus_area VARCHAR(100) NULL,
    location_label VARCHAR(150) NULL,
    requested_role_code VARCHAR(50) NOT NULL DEFAULT 'VENDOR_MANAGER',
    desired_password_hash VARCHAR(255) NULL,
    notes TEXT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    reviewed_by_user_id BIGINT UNSIGNED NULL,
    reviewed_at DATETIME(3) NULL,
    rejection_reason TEXT NULL,
    created_vendor_id BIGINT UNSIGNED NULL,
    created_user_id BIGINT UNSIGNED NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_vendor_signup_contact_email UNIQUE (contact_email),
    CONSTRAINT fk_vendor_signup_reviewed_by FOREIGN KEY (reviewed_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_vendor_signup_created_vendor FOREIGN KEY (created_vendor_id) REFERENCES vendors(id),
    CONSTRAINT fk_vendor_signup_created_user FOREIGN KEY (created_user_id) REFERENCES users(id)
);

CREATE INDEX idx_vendor_signup_status_created_at ON vendor_signup_requests(status, created_at);