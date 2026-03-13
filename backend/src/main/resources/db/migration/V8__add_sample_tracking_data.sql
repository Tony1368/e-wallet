-- Check if sample data already exists and only insert if not
DO $$
BEGIN
    -- Only insert sample data if no sessions exist
    IF NOT EXISTS (SELECT 1 FROM user_session LIMIT 1) THEN
        -- Add sample user session data using sequences
        INSERT INTO user_session (id, session_id, login_time, logout_time, ip_address, user_agent, device_type, browser, operating_system, country, city, region, latitude, longitude, timezone, is_active, user_id) VALUES
        (nextval('user_session_seq'), 'session_001_20240625', '2024-06-25 10:00:00', '2024-06-25 12:30:00', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'Desktop', 'Chrome', 'Windows 10', 'Vietnam', 'Hanoi', 'Hanoi', '21.0285', '105.8542', 'Asia/Ho_Chi_Minh', false, 1),
        (nextval('user_session_seq'), 'session_002_20240625', '2024-06-25 14:00:00', NULL, '192.168.1.101', 'Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15', 'Mobile', 'Safari', 'iOS 15', 'Vietnam', 'Ho Chi Minh City', 'Ho Chi Minh', '10.8231', '106.6297', 'Asia/Ho_Chi_Minh', true, 2),
        (nextval('user_session_seq'), 'session_003_20240625', '2024-06-25 09:15:00', '2024-06-25 11:45:00', '192.168.1.102', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', 'Desktop', 'Firefox', 'macOS', 'Vietnam', 'Da Nang', 'Da Nang', '16.0544', '108.2022', 'Asia/Ho_Chi_Minh', false, 3);
    END IF;
END $$;

-- Check if sample data already exists and only insert if not
DO $$
BEGIN
    -- Only insert sample data if no activities exist
    IF NOT EXISTS (SELECT 1 FROM user_activity LIMIT 1) THEN
        -- Add sample user activity data using sequences
        INSERT INTO user_activity (id, activity_id, activity_time, activity_type, description, amount, from_wallet_iban, to_wallet_iban, ip_address, user_agent, device_type, browser, operating_system, country, city, region, latitude, longitude, timezone, is_successful, error_message, user_id, session_id) VALUES
        (nextval('user_activity_seq'), 'activity_001_20240625', '2024-06-25 10:05:00', 'LOGIN', 'User logged in successfully', NULL, NULL, NULL, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'Desktop', 'Chrome', 'Windows 10', 'Vietnam', 'Hanoi', 'Hanoi', '21.0285', '105.8542', 'Asia/Ho_Chi_Minh', true, NULL, 1, (SELECT id FROM user_session WHERE session_id = 'session_001_20240625' LIMIT 1)),
        (nextval('user_activity_seq'), 'activity_002_20240625', '2024-06-25 10:30:00', 'TRANSFER', 'Transfer funds between wallets', 1000000.00, 'VN123456789012345678901234', 'VN987654321098765432109876', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'Desktop', 'Chrome', 'Windows 10', 'Vietnam', 'Hanoi', 'Hanoi', '21.0285', '105.8542', 'Asia/Ho_Chi_Minh', true, NULL, 1, (SELECT id FROM user_session WHERE session_id = 'session_001_20240625' LIMIT 1)),
        (nextval('user_activity_seq'), 'activity_003_20240625', '2024-06-25 11:00:00', 'ADD_FUNDS', 'Added funds to wallet', 500000.00, NULL, 'VN123456789012345678901234', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'Desktop', 'Chrome', 'Windows 10', 'Vietnam', 'Hanoi', 'Hanoi', '21.0285', '105.8542', 'Asia/Ho_Chi_Minh', true, NULL, 1, (SELECT id FROM user_session WHERE session_id = 'session_001_20240625' LIMIT 1)),
        (nextval('user_activity_seq'), 'activity_004_20240625', '2024-06-25 14:05:00', 'LOGIN', 'User logged in successfully', NULL, NULL, NULL, '192.168.1.101', 'Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15', 'Mobile', 'Safari', 'iOS 15', 'Vietnam', 'Ho Chi Minh City', 'Ho Chi Minh', '10.8231', '106.6297', 'Asia/Ho_Chi_Minh', true, NULL, 2, (SELECT id FROM user_session WHERE session_id = 'session_002_20240625' LIMIT 1)),
        (nextval('user_activity_seq'), 'activity_005_20240625', '2024-06-25 14:15:00', 'WITHDRAW', 'Withdrew funds from wallet', 200000.00, 'VN987654321098765432109876', NULL, '192.168.1.101', 'Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15', 'Mobile', 'Safari', 'iOS 15', 'Vietnam', 'Ho Chi Minh City', 'Ho Chi Minh', '10.8231', '106.6297', 'Asia/Ho_Chi_Minh', true, NULL, 2, (SELECT id FROM user_session WHERE session_id = 'session_002_20240625' LIMIT 1)),
        (nextval('user_activity_seq'), 'activity_006_20240625', '2024-06-25 09:20:00', 'LOGIN', 'User logged in successfully', NULL, NULL, NULL, '192.168.1.102', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', 'Desktop', 'Firefox', 'macOS', 'Vietnam', 'Da Nang', 'Da Nang', '16.0544', '108.2022', 'Asia/Ho_Chi_Minh', true, NULL, 3, (SELECT id FROM user_session WHERE session_id = 'session_003_20240625' LIMIT 1)),
        (nextval('user_activity_seq'), 'activity_007_20240625', '2024-06-25 09:30:00', 'TRANSFER', 'Transfer funds between wallets', 750000.00, 'VN111111111111111111111111', 'VN222222222222222222222222', '192.168.1.102', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', 'Desktop', 'Firefox', 'macOS', 'Vietnam', 'Da Nang', 'Da Nang', '16.0544', '108.2022', 'Asia/Ho_Chi_Minh', false, 'Insufficient funds', 3, (SELECT id FROM user_session WHERE session_id = 'session_003_20240625' LIMIT 1));
    END IF;
END $$; 