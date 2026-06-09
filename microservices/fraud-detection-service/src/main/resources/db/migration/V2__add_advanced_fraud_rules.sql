-- Them cac cot cau hinh fraud nang cao
ALTER TABLE fraud_rule_config
    ADD COLUMN IF NOT EXISTS geo_velocity_minutes INTEGER NOT NULL DEFAULT 30,
    ADD COLUMN IF NOT EXISTS geo_velocity_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS max_transactions_per_minute INTEGER NOT NULL DEFAULT 10,
    ADD COLUMN IF NOT EXISTS velocity_window_seconds INTEGER NOT NULL DEFAULT 60,
    ADD COLUMN IF NOT EXISTS anomaly_amount_multiplier NUMERIC(5,2) NOT NULL DEFAULT 3.00,
    ADD COLUMN IF NOT EXISTS anomaly_enabled BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE fraud_rule_config SET
    geo_velocity_minutes = 30,
    geo_velocity_enabled = true,
    max_transactions_per_minute = 10,
    velocity_window_seconds = 60,
    anomaly_amount_multiplier = 3.00,
    anomaly_enabled = true
WHERE rule_name = 'DEFAULT_RULE';
