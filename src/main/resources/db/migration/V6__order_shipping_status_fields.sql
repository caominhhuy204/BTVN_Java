ALTER TABLE orders
    ADD COLUMN shipping_name VARCHAR(255),
    ADD COLUMN shipping_address TEXT,
    ADD COLUMN shipping_phone VARCHAR(50),
    ADD COLUMN payment_method VARCHAR(50),
    ADD COLUMN shipping_fee DOUBLE NOT NULL DEFAULT 0,
    ADD COLUMN discount DOUBLE NOT NULL DEFAULT 0,
    ADD COLUMN admin_note TEXT,
    ADD COLUMN status_updated_at TIMESTAMP NULL;
