package com.venomgrave.hexvg.impl.database;

import com.venomgrave.hexvg.api.database.DatabaseService;
import com.venomgrave.hexvg.api.database.DatabaseType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MigrationRunner {

    private static final String SCHEMA_TABLE    = "hexvg_schema_version";
    private static final String MIGRATIONS_ROOT = "/migrations/";

    private final DatabaseService db;
    private final Logger          logger;

    // Subfolder wewnątrz /migrations/ — np. "core", "playertools"
    private final String subFolder;

    public MigrationRunner(DatabaseService db, Logger logger, String subFolder) {
        this.db        = db;
        this.logger    = logger;
        this.subFolder = subFolder.endsWith("/") ? subFolder : subFolder + "/";
    }

    // ── Publiczne API ─────────────────────────────────────────────────────

    /**
     * Uruchamia migracje z podanej listy plików.
     * Pliki muszą znajdować się w resources/migrations/{subFolder}/
     *
     * Każdy plik jest wykonywany tylko raz — wyniki zapisywane
     * w tabeli hexvg_schema_version.
     *
     * @param files lista nazw plików SQL, np. ["V1__init.sql", "V2__add_col.sql"]
     */
    public void run(List<String> files) {
        try (Connection conn = db.getConnection()) {
            ensureSchemaTable(conn);

            List<String> pending = resolvePending(conn, files);

            if (pending.isEmpty()) {
                logger.info("[Migration:" + subFolder.strip() + "] "
                        + "Brak nowych migracji.");
                return;
            }

            logger.info("[Migration:" + subFolder.strip() + "] "
                    + "Znaleziono " + pending.size() + " nowych migracji.");

            for (String file : pending) {
                executeMigration(conn, file);
            }

            logger.info("[Migration:" + subFolder.strip() + "] "
                    + "Wszystkie migracje zakończone.");

        } catch (SQLException e) {
            logger.severe("[Migration] Błąd połączenia: " + e.getMessage());
            throw new RuntimeException("Migration failed", e);
        }
    }

    // ── Prywatne ─────────────────────────────────────────────────────────

    /**
     * Tworzy tabelę wersji jeśli nie istnieje.
     * Wspólna dla wszystkich pluginów — przechowuje prefix subFolder.
     */
    private void ensureSchemaTable(Connection conn) throws SQLException {
        String sql;

        if (db.getType() == DatabaseType.MYSQL) {
            sql = """
                    CREATE TABLE IF NOT EXISTS hexvg_schema_version (
                        id         INT          NOT NULL AUTO_INCREMENT,
                        module     VARCHAR(50)  NOT NULL,
                        version    VARCHAR(100) NOT NULL,
                        applied_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (id),
                        UNIQUE KEY uq_module_version (module, version)
                    )
                    """;
        } else {
            sql = """
                    CREATE TABLE IF NOT EXISTS hexvg_schema_version (
                        id         INTEGER      PRIMARY KEY AUTOINCREMENT,
                        module     TEXT         NOT NULL,
                        version    TEXT         NOT NULL,
                        applied_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE (module, version)
                    )
                    """;
        }

        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }

    /**
     * Zwraca listę plików które jeszcze nie były wykonane,
     * posortowaną naturalnie (V1 → V2 → V3...).
     */
    private List<String> resolvePending(Connection conn,
                                        List<String> all) throws SQLException {
        String moduleName = subFolder.replace("/", "");
        String sql = "SELECT version FROM " + SCHEMA_TABLE
                + " WHERE module = ?";

        List<String> applied = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, moduleName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    applied.add(rs.getString("version"));
                }
            }
        }

        return all.stream()
                .filter(f -> !applied.contains(f))
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    /**
     * Wykonuje pojedynczy plik SQL i zapisuje go jako zastosowany.
     */
    private void executeMigration(Connection conn, String fileName) throws SQLException {
        String resourcePath = MIGRATIONS_ROOT + subFolder + fileName;
        String content      = readResource(resourcePath);

        if (content == null) {
            logger.severe("[Migration] Nie można odczytać: " + resourcePath);
            throw new RuntimeException("Missing migration file: " + resourcePath);
        }

        if (content.isBlank()) {
            logger.warning("[Migration] Pusty plik: " + fileName + " — pomijam.");
            markApplied(conn, fileName);
            return;
        }

        logger.info("[Migration] Wykonuję: " + fileName);

        // Splitujemy po średniku — każde polecenie osobno
        String[] statements = content.split(";");

        try {
            // Wyłącz autocommit — cały plik jako jedna transakcja
            conn.setAutoCommit(false);

            try (Statement st = conn.createStatement()) {
                for (String stmt : statements) {
                    String trimmed = stmt.strip();
                    // Pomijamy komentarze i puste linie
                    if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;
                    st.execute(trimmed);
                }
            }

            markApplied(conn, fileName);
            conn.commit();

            logger.info("[Migration] Zakończono: " + fileName);

        } catch (SQLException e) {
            logger.severe("[Migration] Błąd w " + fileName + ": " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                logger.warning("[Migration] Rollback failed: " + ex.getMessage());
            }
            throw new RuntimeException("Migration failed: " + fileName, e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                logger.warning("[Migration] autoCommit restore failed: " + e.getMessage());
            }
        }
    }

    /**
     * Zapisuje wykonaną migrację do hexvg_schema_version.
     */
    private void markApplied(Connection conn, String fileName) throws SQLException {
        String moduleName = subFolder.replace("/", "");
        String sql;

        if (db.getType() == DatabaseType.MYSQL) {
            sql = "INSERT IGNORE INTO " + SCHEMA_TABLE
                    + " (module, version) VALUES (?, ?)";
        } else {
            sql = "INSERT OR IGNORE INTO " + SCHEMA_TABLE
                    + " (module, version) VALUES (?, ?)";
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, moduleName);
            ps.setString(2, fileName);
            ps.executeUpdate();
        }
    }

    /**
     * Odczytuje plik z resources JAR.
     * Zwraca null jeśli plik nie istnieje.
     */
    private String readResource(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                logger.severe("[Migration] Brak pliku w JAR: " + path);
                return null;
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines()
                        .collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            logger.severe("[Migration] Błąd odczytu " + path + ": " + e.getMessage());
            return null;
        }
    }
}