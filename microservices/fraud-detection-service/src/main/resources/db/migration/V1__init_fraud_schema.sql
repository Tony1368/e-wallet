CREATE TABLE fraud_rule_config (
    id BIGSERIAL PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL UNIQUE,
    max_transaction_amount NUMERIC(19,2) NOT NULL,
    max_daily_transactions INTEGER NOT NULL,
    max_daily_amount NUMERIC(19,2) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO fraud_rule_config (rule_name, max_transaction_amount, max_daily_transactions, max_daily_amount, enabled)
VALUES ('DEFAULT_RULE', 50000000.00, 100, 200000000.00, true);
