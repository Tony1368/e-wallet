CREATE TABLE refund_request (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL,
    wallet_id BIGINT NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_by VARCHAR(100),
    approved_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX idx_refund_request_status ON refund_request(status);
