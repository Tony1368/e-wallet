CREATE TABLE audit_log (
    id              BIGSERIAL PRIMARY KEY,
    request_id      VARCHAR(36) NOT NULL,
    service_name    VARCHAR(50) NOT NULL,
    http_method     VARCHAR(10),
    path            VARCHAR(500),
    status_code     INTEGER,
    duration_ms     BIGINT,
    username        VARCHAR(100),
    client_ip       VARCHAR(45),
    caller_service  VARCHAR(50),
    request_body    VARCHAR(1000),
    response_body   VARCHAR(1000),
    level           VARCHAR(10) NOT NULL DEFAULT 'INFO',
    error_class     VARCHAR(200),
    timestamp       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_request_id ON audit_log(request_id);
CREATE INDEX idx_audit_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_service ON audit_log(service_name);
CREATE INDEX idx_audit_user ON audit_log(username);
CREATE INDEX idx_audit_level_time ON audit_log(level, timestamp);
