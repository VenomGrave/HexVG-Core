CREATE TABLE IF NOT EXISTS hexvg_audit (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    actor_uuid  VARCHAR(36)  NOT NULL,
    actor_name  VARCHAR(16)  NOT NULL,
    action      VARCHAR(50)  NOT NULL,
    target      VARCHAR(100),
    data        TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_audit_actor  (actor_uuid),
    INDEX idx_audit_action (action),
    INDEX idx_audit_date   (created_at)
)