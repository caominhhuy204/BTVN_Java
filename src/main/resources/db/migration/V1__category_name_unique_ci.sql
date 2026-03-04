-- Ensure case-insensitive unique name for category
ALTER TABLE category
    ADD COLUMN IF NOT EXISTS name_lower VARCHAR(255) GENERATED ALWAYS AS (lower(name)) STORED;

CREATE UNIQUE INDEX IF NOT EXISTS uq_category_name_ci ON category (name_lower);
