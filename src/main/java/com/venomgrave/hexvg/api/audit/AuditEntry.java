package com.venomgrave.hexvg.api.audit;

import java.time.Instant;
import java.util.UUID;

/**
 * Pojedynczy wpis w logu audytowym.
 */
public record AuditEntry(
        UUID        actorUuid,
        String      actorName,
        AuditAction action,
        String      target,
        String      data,
        Instant     timestamp
) {
    /** Skrót — bez danych, timestamp = teraz. */
    public AuditEntry(UUID actorUuid, String actorName,
                      AuditAction action, String target) {
        this(actorUuid, actorName, action, target, null, Instant.now());
    }

    /** Skrót — z danymi, timestamp = teraz. */
    public AuditEntry(UUID actorUuid, String actorName,
                      AuditAction action, String target, String data) {
        this(actorUuid, actorName, action, target, data, Instant.now());
    }
}