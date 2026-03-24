CREATE TABLE menu_categories (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    vendor_id BIGINT UNSIGNED NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    source_system VARCHAR(40) NOT NULL DEFAULT 'VENDOR_OPS',
    external_category_id VARCHAR(64) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_menu_categories_vendor
        FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    CONSTRAINT uk_menu_categories_vendor_name UNIQUE (vendor_id, category_name)
);

CREATE TABLE menu_items (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    vendor_id BIGINT UNSIGNED NOT NULL,
    category_id BIGINT UNSIGNED NULL,
    item_code VARCHAR(40) NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    price DECIMAL(10,2) NOT NULL,
    cost_price DECIMAL(10,2) NULL,
    prep_time_minutes SMALLINT UNSIGNED NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_veg BOOLEAN NOT NULL DEFAULT TRUE,
    track_inventory BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 0,
    primary_image_url VARCHAR(500) NULL,
    source_system VARCHAR(40) NOT NULL DEFAULT 'VENDOR_OPS',
    external_menu_item_id VARCHAR(64) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_menu_items_vendor
        FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    CONSTRAINT fk_menu_items_category
        FOREIGN KEY (category_id) REFERENCES menu_categories(id),
    CONSTRAINT uk_menu_items_vendor_code UNIQUE (vendor_id, item_code)
);

CREATE TABLE menu_item_media (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    menu_item_id BIGINT UNSIGNED NOT NULL,
    media_type VARCHAR(20) NOT NULL DEFAULT 'IMAGE',
    media_url VARCHAR(500) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_menu_item_media_menu_item
        FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

CREATE INDEX idx_menu_categories_vendor_id ON menu_categories(vendor_id);
CREATE INDEX idx_menu_categories_display_order ON menu_categories(vendor_id, display_order);
CREATE INDEX idx_menu_items_vendor_id ON menu_items(vendor_id);
CREATE INDEX idx_menu_items_category_id ON menu_items(category_id);
CREATE INDEX idx_menu_items_available ON menu_items(vendor_id, is_available);
CREATE INDEX idx_menu_item_media_menu_item_id ON menu_item_media(menu_item_id);