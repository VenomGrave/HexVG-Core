CREATE TABLE IF NOT EXISTS hexvg_warnings (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    player_uuid  VARCHAR(36)  NOT NULL,
    player_name  VARCHAR(16)  NOT NULL,
    staff_uuid   VARCHAR(36)  NOT NULL,
    staff_name   VARCHAR(16)  NOT NULL,
    reason       VARCHAR(255) NOT NULL,
    issued_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at   TIMESTAMP    NULL,
    active       TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    INDEX idx_warn_player (player_uuid),
    INDEX idx_warn_active (active)
);

CREATE TABLE IF NOT EXISTS hexvg_punishments (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    player_uuid  VARCHAR(36)  NOT NULL,
    player_name  VARCHAR(16)  NOT NULL,
    type         VARCHAR(20)  NOT NULL,
    staff_uuid   VARCHAR(36)  NOT NULL,
    staff_name   VARCHAR(16)  NOT NULL,
    reason       VARCHAR(255) NOT NULL,
    issued_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at   TIMESTAMP    NULL,
    active       TINYINT(1)   NOT NULL DEFAULT 1,
    ip           VARCHAR(45)  NULL,
    PRIMARY KEY (id),
    INDEX idx_punish_player (player_uuid),
    INDEX idx_punish_type   (type),
    INDEX idx_punish_active (active)
)