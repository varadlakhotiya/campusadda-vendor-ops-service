CREATE TABLE inventory_items (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    vendor_id BIGINT UNSIGNED NOT NULL,
    item_code VARCHAR(40) NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    unit VARCHAR(20) NOT NULL,
    current_quantity DECIMAL(14,3) NOT NULL DEFAULT 0,
    reserved_quantity DECIMAL(14,3) NOT NULL DEFAULT 0,
    low_stock_threshold DECIMAL(14,3) NOT NULL DEFAULT 0,
    max_stock_level DECIMAL(14,3) NULL,
    unit_cost DECIMAL(10,2) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    last_restocked_at DATETIME(3) NULL,
    source_system VARCHAR(40) NOT NULL DEFAULT 'VENDOR_OPS',
    external_inventory_item_id VARCHAR(64) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_inventory_items_vendor
        FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    CONSTRAINT uk_inventory_items_vendor_code UNIQUE (vendor_id, item_code)
);

CREATE TABLE inventory_policies (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    inventory_item_id BIGINT UNSIGNED NOT NULL,
    lead_time_days INT UNSIGNED NOT NULL DEFAULT 1,
    review_period_days INT UNSIGNED NOT NULL DEFAULT 1,
    service_level_pct DECIMAL(5,2) NOT NULL DEFAULT 95.00,
    safety_stock_qty DECIMAL(14,3) NULL,
    reorder_point_qty DECIMAL(14,3) NULL,
    min_reorder_qty DECIMAL(14,3) NULL,
    max_reorder_qty DECIMAL(14,3) NULL,
    preferred_model VARCHAR(50) NULL,
    auto_recommend_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_inventory_policies_item
        FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id),
    CONSTRAINT uk_inventory_policies_item UNIQUE (inventory_item_id)
);

CREATE TABLE menu_item_ingredients (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    menu_item_id BIGINT UNSIGNED NOT NULL,
    inventory_item_id BIGINT UNSIGNED NOT NULL,
    quantity_required DECIMAL(14,3) NOT NULL,
    wastage_pct DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    is_optional BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_menu_item_ingredients_menu_item
        FOREIGN KEY (menu_item_id) REFERENCES menu_items(id),
    CONSTRAINT fk_menu_item_ingredients_inventory_item
        FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id),
    CONSTRAINT uk_menu_item_ingredients UNIQUE (menu_item_id, inventory_item_id)
);

CREATE TABLE stock_movements (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    vendor_id BIGINT UNSIGNED NOT NULL,
    inventory_item_id BIGINT UNSIGNED NOT NULL,
    movement_type VARCHAR(32) NOT NULL,
    reference_type VARCHAR(32) NULL,
    reference_id BIGINT UNSIGNED NULL,
    quantity_delta DECIMAL(14,3) NOT NULL,
    quantity_before DECIMAL(14,3) NOT NULL,
    quantity_after DECIMAL(14,3) NOT NULL,
    unit_cost DECIMAL(10,2) NULL,
    reason VARCHAR(255) NULL,
    details_json JSON NULL,
    created_by_user_id BIGINT UNSIGNED NULL,
    event_time DATETIME(3) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_stock_movements_vendor
        FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    CONSTRAINT fk_stock_movements_inventory_item
        FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id),
    CONSTRAINT fk_stock_movements_user
        FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_inventory_items_vendor_id ON inventory_items(vendor_id);
CREATE INDEX idx_inventory_items_status ON inventory_items(vendor_id, status);
CREATE INDEX idx_inventory_items_name ON inventory_items(vendor_id, item_name);
CREATE INDEX idx_menu_item_ingredients_menu_item_id ON menu_item_ingredients(menu_item_id);
CREATE INDEX idx_menu_item_ingredients_inventory_item_id ON menu_item_ingredients(inventory_item_id);
CREATE INDEX idx_stock_movements_inventory_item_id ON stock_movements(inventory_item_id, event_time);
CREATE INDEX idx_stock_movements_vendor_id ON stock_movements(vendor_id, event_time);