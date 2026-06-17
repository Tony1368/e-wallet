CREATE TABLE branch (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    address VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE wallet ADD COLUMN branch_id BIGINT REFERENCES branch(id);

CREATE INDEX idx_wallet_branch_id ON wallet(branch_id);

-- Seed data
INSERT INTO branch (name, code, address) VALUES
('Hội sở chính', 'HQ', 'Tầng 5, Tòa nhà HUST'),
('Chi nhánh Trần Đại Nghĩa', 'TDN', '1 Trần Đại Nghĩa, Hai Bà Trưng, Hà Nội'),
('Chi nhánh Bách Khoa', 'BK', 'C1-201, ĐHBK Hà Nội');

-- Assign wallets to branches
UPDATE wallet SET branch_id = 1 WHERE user_id IN (1, 6); -- admin, ketoan -> HQ
UPDATE wallet SET branch_id = 2 WHERE user_id IN (5, 7); -- quanly, pos -> TDN
UPDATE wallet SET branch_id = 2 WHERE user_id = 4; -- nhanvien -> TDN
