CREATE SEQUENCE IF NOT EXISTS user_session_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS user_activity_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE user_session
(
    id           BIGINT                      NOT NULL,
    session_id   VARCHAR(255)                NOT NULL,
    login_time   TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    logout_time  TIMESTAMP WITHOUT TIME ZONE DEFAULT NULL,
    ip_address   VARCHAR(45),
    user_agent   VARCHAR(500),
    device_type  VARCHAR(100),
    browser      VARCHAR(100),
    operating_system VARCHAR(100),
    country      VARCHAR(100),
    city         VARCHAR(100),
    region       VARCHAR(100),
    latitude     VARCHAR(20),
    longitude    VARCHAR(20),
    timezone     VARCHAR(50),
    is_active    BOOLEAN                     NOT NULL DEFAULT TRUE,
    user_id      BIGINT                      NOT NULL,
    CONSTRAINT pk_user_session PRIMARY KEY (id)
);

CREATE TABLE user_activity
(
    id              BIGINT                      NOT NULL,
    activity_id     VARCHAR(255)                NOT NULL,
    activity_time   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    activity_type   VARCHAR(50)                 NOT NULL,
    description     VARCHAR(500),
    amount          DECIMAL,
    from_wallet_iban VARCHAR(34),
    to_wallet_iban  VARCHAR(34),
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    device_type     VARCHAR(100),
    browser         VARCHAR(100),
    operating_system VARCHAR(100),
    country         VARCHAR(100),
    city            VARCHAR(100),
    region          VARCHAR(100),
    latitude        VARCHAR(20),
    longitude       VARCHAR(20),
    timezone        VARCHAR(50),
    is_successful   BOOLEAN                     NOT NULL DEFAULT TRUE,
    error_message   VARCHAR(500),
    user_id         BIGINT                      NOT NULL,
    session_id      BIGINT,
    CONSTRAINT pk_user_activity PRIMARY KEY (id)
);

ALTER TABLE user_session ADD CONSTRAINT uc_user_session_session_id UNIQUE (session_id);
ALTER TABLE user_activity ADD CONSTRAINT uc_user_activity_activity_id UNIQUE (activity_id);

ALTER TABLE user_session
    ADD CONSTRAINT fk_user_session_on_user FOREIGN KEY (user_id) REFERENCES public."user" (id);

ALTER TABLE user_activity
    ADD CONSTRAINT fk_user_activity_on_user FOREIGN KEY (user_id) REFERENCES public."user" (id);

ALTER TABLE user_activity
    ADD CONSTRAINT fk_user_activity_on_session FOREIGN KEY (session_id) REFERENCES user_session (id);

CREATE INDEX idx_user_session_user_id ON user_session (user_id);
CREATE INDEX idx_user_session_login_time ON user_session (login_time);
CREATE INDEX idx_user_session_is_active ON user_session (is_active);

CREATE INDEX idx_user_activity_user_id ON user_activity (user_id);
CREATE INDEX idx_user_activity_activity_time ON user_activity (activity_time);
CREATE INDEX idx_user_activity_activity_type ON user_activity (activity_type);
CREATE INDEX idx_user_activity_session_id ON user_activity (session_id);
