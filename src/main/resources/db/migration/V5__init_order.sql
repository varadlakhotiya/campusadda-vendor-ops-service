CREATE TABLE orders (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    vendor_id BIGINT UNSIGNED NOT NULL,
    order_number VARCHAR(40) NOT NULL,
    source_system VARCHAR(40) NOT NULL DEFAULT 'VENDOR_OPS',
    external_order_id VARCHAR(64) NULL,
    external_customer_id VARCHAR(64) NULL,
    order_source VARCHAR(32) NOT NULL DEFAULT 'APP',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    payment_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(32) NULL,
    customer_name VARCHAR(120) NULL,
    customer_phone VARCHAR(20) NULL,
    subtotal_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    notes TEXT NULL,
    pickup_eta_at DATETIME(3) NULL,
    placed_at DATETIME(3) NOT NULL,
    accepted_at DATETIME(3) NULL,
    preparing_at DATETIME(3) NULL,
    ready_at DATETIME(3) NULL,
    completed_at DATETIME(3) NULL,
    cancelled_at DATETIME(3) NULL,
    created_by_user_id BIGINT UNSIGNED NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_orders_order_number UNIQUE (order_number),
    CONSTRAINT fk_orders_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    CONSTRAINT fk_orders_created_by_user FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);

CREATE TABLE order_items (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id BIGINT UNSIGNED NOT NULL,
    menu_item_id BIGINT UNSIGNED NULL,
    item_name_snapshot VARCHAR(150) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    quantity INT UNSIGNED NOT NULL,
    line_total DECIMAL(10,2) NOT NULL,
    special_instructions VARCHAR(255) NULL,
    recipe_snapshot_json JSON NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_items_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

CREATE TABLE order_status_history (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id BIGINT UNSIGNED NOT NULL,
    from_status VARCHAR(32) NULL,
    to_status VARCHAR(32) NOT NULL,
    changed_by_user_id BIGINT UNSIGNED NULL,
    remarks VARCHAR(255) NULL,
    changed_at DATETIME(3) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_order_status_history_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_status_history_user FOREIGN KEY (changed_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_orders_vendor_status ON orders(vendor_id, status);
CREATE INDEX idx_orders_vendor_placed_at ON orders(vendor_id, placed_at);
CREATE INDEX idx_orders_external_order ON orders(source_system, external_order_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_status_history_order_id ON order_status_history(order_id, changed_at);