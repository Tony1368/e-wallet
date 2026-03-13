-- Create fraud_rule_config table
CREATE TABLE fraud_rule_config (
    id BIGSERIAL PRIMARY KEY,
    rule_key VARCHAR(100) UNIQUE NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    description TEXT
);

-- Insert default fraud rule configurations
INSERT INTO fraud_rule_config (rule_key, rule_name, value, description) VALUES
('max_transactions_per_minute', 'Số giao dịch tối đa mỗi phút', '5', 'Maximum number of transactions allowed per minute per user'),
('rapid_location_change_threshold_minutes', 'Ngưỡng thay đổi vị trí nhanh (phút)', '30', 'Time threshold in minutes for detecting rapid location changes'),
('unusual_amount_multiplier', 'Hệ số phát hiện số tiền bất thường', '3', 'Multiplier for detecting unusually large transaction amounts compared to user average'),
('max_amount_threshold', 'Ngưỡng số tiền tối đa (VND)', '1000000', 'Maximum transaction amount threshold in VND'); 