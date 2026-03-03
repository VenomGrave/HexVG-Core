package com.venomgrave.hexvg.impl.audit;

import com.venomgrave.hexvg.api.audit.AuditEntry;
import com.venomgrave.hexvg.api.audit.AuditLogger;
import com.venomgrave.hexvg.api.database.DatabaseService;
import com.venomgrave.hexvg.config.CoreConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class DatabaseAuditLogger implements AuditLogger {

    private static final String INSERT_SQL = """
            INSERT INTO hexvg_audit
                (actor_uuid, actor_name, action, target, data, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private final DatabaseService db;
    private final Logger          logger;
    private final CoreConfig      config;

    // Dedykowany jednowątkowy executor — audit nie blokuje main thread
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "HexVG-Audit-Thread");
        t.setDaemon(true);
        return t;
    });

    public DatabaseAuditLogger(DatabaseService db,
                               Logger logger,
                               CoreConfig config) {
        this.db     = db;
        this.logger = logger;
        this.config = config;
    }

    // ── AuditLogger ───────────────────────────────────────────────────────

    @Override
    public void log(AuditEntry entry) {
        executor.submit(() -> persist(entry));
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        try {
            // Daj 5 sekund na dokończenie oczekujących logów
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warning("[Audit] Timeout przy zamykaniu — część logów mogła zaginąć.");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ── Prywatne ─────────────────────────────────────────────────────────

    private void persist(AuditEntry entry) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setString(   1, entry.actorUuid().toString());
            ps.setString(   2, entry.actorName());
            ps.setString(   3, entry.action().name());
            ps.setString(   4, entry.target());
            ps.setString(   5, entry.data());
            ps.setTimestamp(6, Timestamp.from(entry.timestamp()));
            ps.executeUpdate();

            if (config.isDebug()) {
                logger.info("[Audit] "
                        + entry.actorName()
                        + " → " + entry.action().name()
                        + " | target: " + entry.target()
                        + (entry.data() != null ? " | " + entry.data() : ""));
            }

        } catch (SQLException e) {
            logger.warning("[Audit] Błąd zapisu: " + e.getMessage());
        }
    }
}