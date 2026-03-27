CREATE TABLE etl_job_runs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    job_name VARCHAR(100) NOT NULL,
    run_type VARCHAR(32) NOT NULL,
    window_start DATETIME(3) NOT NULL,
    window_end DATETIME(3) NOT NULL,
    status VARCHAR(32) NOT NULL,
    records_processed INT UNSIGNED NOT NULL DEFAULT 0,
    error_message TEXT NULL,
    started_at DATETIME(3) NOT NULL,
    finished_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id)
);

CREATE TABLE daily_item_sales (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    sales_date DATE NOT NULL,
    vendor_id BIGINT UNSIGNED NOT NULL,
    menu_item_id BIGINT UNSIGNED NOT NULL,
    quantity_sold INT UNSIGNED NOT NULL DEFAULT 0,
    order_count INT UNSIGNED NOT NULL DEFAULT 0,
    gross_revenue DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    net_revenue DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    avg_selling_price DECIMAL(10,2) NULL,
    first_order_at DATETIME(3) NULL,
    last_order_at DATETIME(3) NULL,
    etl_run_id BIGINT UNSIGNED NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_daily_item_sales UNIQUE (sales_date, vendor_id, menu_item_id),
    CONSTRAINT fk_daily_item_sales_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    CONSTRAINT fk_daily_item_sales_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id),
    CONSTRAINT fk_daily_item_sales_etl FOREIGN KEY (etl_run_id) REFERENCES etl_job_runs(id)
);

CREATE TABLE daily_vendor_sales (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    sales_date DATE NOT NULL,
    vendor_id BIGINT UNSIGNED NOT NULL,
    total_orders INT UNSIGNED NOT NULL DEFAULT 0,
    completed_orders INT UNSIGNED NOT NULL DEFAULT 0,
    cancelled_orders INT UNSIGNED NOT NULL DEFAULT 0,
    items_sold_qty INT UNSIGNED NOT NULL DEFAULT 0,
    gross_revenue DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    net_revenue DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    avg_order_value DECIMAL(10,2) NULL,
    etl_run_id BIGINT UNSIGNED NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_daily_vendor_sales UNIQUE (sales_date, vendor_id),
    CONSTRAINT fk_daily_vendor_sales_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    CONSTRAINT fk_daily_vendor_sales_etl FOREIGN KEY (etl_run_id) REFERENCES etl_job_runs(id)
);

CREATE TABLE hourly_vendor_sales (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    sales_date DATE NOT NULL,
    vendor_id BIGINT UNSIGNED NOT NULL,
    sales_hour TINYINT UNSIGNED NOT NULL,
    total_orders INT UNSIGNED NOT NULL DEFAULT 0,
    items_sold_qty INT UNSIGNED NOT NULL DEFAULT 0,
    revenue DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    etl_run_id BIGINT UNSIGNED NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_hourly_vendor_sales UNIQUE (sales_date, vendor_id, sales_hour),
    CONSTRAINT fk_hourly_vendor_sales_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    CONSTRAINT fk_hourly_vendor_sales_etl FOREIGN KEY (etl_run_id) REFERENCES etl_job_runs(id)
);

CREATE INDEX idx_etl_job_runs_name_started ON etl_job_runs(job_name, started_at);
CREATE INDEX idx_etl_job_runs_status ON etl_job_runs(status);
CREATE INDEX idx_daily_item_sales_vendor_date ON daily_item_sales(vendor_id, sales_date);
CREATE INDEX idx_daily_item_sales_item_date ON daily_item_sales(menu_item_id, sales_date);
CREATE INDEX idx_daily_vendor_sales_vendor_date ON daily_vendor_sales(vendor_id, sales_date);
CREATE INDEX idx_hourly_vendor_sales_vendor_date ON hourly_vendor_sales(vendor_id, sales_date);