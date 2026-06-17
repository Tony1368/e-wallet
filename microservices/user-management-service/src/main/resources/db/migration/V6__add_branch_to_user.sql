ALTER TABLE "user" ADD COLUMN branch_id BIGINT;

-- Assign users to branches
UPDATE "user" SET branch_id = 1 WHERE id IN (1, 2, 6); -- admin, thailq, ketoan -> HQ
UPDATE "user" SET branch_id = 2 WHERE id IN (5, 7, 4); -- quanly, pos, nhanvien -> TDN
UPDATE "user" SET branch_id = 3 WHERE id = 3; -- congthanh -> BK
