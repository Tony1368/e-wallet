ALTER TABLE wallet ADD COLUMN status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE';
COMMENT ON COLUMN wallet.status IS 'The status of the wallet (e.g., ACTIVE, CLOSED)'; 