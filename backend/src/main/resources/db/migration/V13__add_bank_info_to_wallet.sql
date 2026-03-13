ALTER TABLE wallet ADD COLUMN bank_info VARCHAR(255);
COMMENT ON COLUMN wallet.bank_info IS 'The bank information associated with the wallet'; 