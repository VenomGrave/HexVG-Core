package com.venomgrave.hexvg.api.audit;

import java.util.UUID;

public interface AuditLogger {

    /**
     * Zapisuje wpis asynchronicznie.
     * Nigdy nie blokuje wątku głównego.
     */
    void log(AuditEntry entry);

    /**
     * Skrót — aktor + akcja + target.
     */
    default void log(UUID actorUuid, String actorName,
                     AuditAction action, String target) {
        log(new AuditEntry(actorUuid, actorName, action, target));
    }

    /**
     * Skrót — z dodatkowymi danymi.
     */
    default void log(UUID actorUuid, String actorName,
                     AuditAction action, String target, String data) {
        log(new AuditEntry(actorUuid, actorName, action, target, data));
    }

    /**
     * Flush i zamknięcie — wywoływane w onDisable().
     * Czeka na dokończenie oczekujących wpisów.
     */
    void shutdown();
}