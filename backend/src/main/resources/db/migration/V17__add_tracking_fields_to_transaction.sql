-- Add tracking information fields to transaction table
ALTER TABLE transaction ADD COLUMN ip_address VARCHAR(45);
ALTER TABLE transaction ADD COLUMN user_agent VARCHAR(500);
ALTER TABLE transaction ADD COLUMN device_type VARCHAR(100);
ALTER TABLE transaction ADD COLUMN browser VARCHAR(100);
ALTER TABLE transaction ADD COLUMN operating_system VARCHAR(100);
ALTER TABLE transaction ADD COLUMN country VARCHAR(100);
ALTER TABLE transaction ADD COLUMN city VARCHAR(100);
ALTER TABLE transaction ADD COLUMN region VARCHAR(100);
ALTER TABLE transaction ADD COLUMN latitude VARCHAR(20);
ALTER TABLE transaction ADD COLUMN longitude VARCHAR(20);
ALTER TABLE transaction ADD COLUMN timezone VARCHAR(50); 