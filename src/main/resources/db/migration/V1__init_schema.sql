-- =============================================
-- V1: Initial schema
-- =============================================

CREATE TABLE users (
    id              BIGSERIAL       PRIMARY KEY,
    first_name      VARCHAR(100)    NOT NULL,
    last_name       VARCHAR(100)    NOT NULL,
    email           VARCHAR(255)    NOT NULL,
    password        VARCHAR(255)    NOT NULL,
    role            VARCHAR(20)     NOT NULL,
    email_verified  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    created_date    TIMESTAMP,
    last_modified_date TIMESTAMP,
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    version         BIGINT
);

CREATE UNIQUE INDEX uk_users_email ON users (LOWER(email));

-- =============================================

CREATE TABLE refresh_tokens (
    id              BIGSERIAL           PRIMARY KEY,
    token           VARCHAR(255)        NOT NULL UNIQUE,
    user_id         BIGINT              NOT NULL REFERENCES users(id),
    expiry_date     TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    created_date    TIMESTAMP,
    last_modified_date TIMESTAMP,
    is_deleted      BOOLEAN             NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN             NOT NULL DEFAULT TRUE,
    version         BIGINT
);

CREATE INDEX idx_refresh_token ON refresh_tokens(token);

-- =============================================

CREATE TABLE audit_logs (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT,
    email           VARCHAR(255),
    action          VARCHAR(100)    NOT NULL,
    http_method     VARCHAR(10)     NOT NULL,
    request_uri     VARCHAR(500)    NOT NULL,
    status_code     INTEGER,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    duration_ms     BIGINT,
    timestamp       TIMESTAMP       NOT NULL
);

CREATE INDEX idx_audit_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);

-- =============================================

CREATE TABLE email_verification_tokens (
    id              BIGSERIAL           PRIMARY KEY,
    token           VARCHAR(255)        NOT NULL UNIQUE,
    user_id         BIGINT              NOT NULL REFERENCES users(id),
    expiry_date     TIMESTAMP WITH TIME ZONE NOT NULL,
    used            BOOLEAN             NOT NULL DEFAULT FALSE,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    created_date    TIMESTAMP,
    last_modified_date TIMESTAMP,
    is_deleted      BOOLEAN             NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN             NOT NULL DEFAULT TRUE,
    version         BIGINT
);

CREATE INDEX idx_email_verification_token ON email_verification_tokens(token);
