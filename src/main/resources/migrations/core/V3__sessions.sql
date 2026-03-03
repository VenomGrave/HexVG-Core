CREATE TABLE IF NOT EXISTS hexvg_sessions (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    player_uuid  VARCHAR(36)  NOT NULL,
    player_name  VARCHAR(16)  NOT NULL,
    login_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    logout_at    TIMESTAMP    NULL,
    ip_hash      VARCHAR(16)  NULL,
    PRIMARY KEY (id),
    INDEX idx_sessions_uuid  (player_uuid),
    INDEX idx_sessions_login (login_at)
);

CREATE TABLE IF NOT EXISTS hexvg_playtime (
    uuid          VARCHAR(36)  NOT NULL,
    total_seconds BIGINT       NOT NULL DEFAULT 0,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (uuid)
)