package com.venomgrave.hexvg.impl.database;

import com.venomgrave.hexvg.api.database.DatabaseService;
import com.venomgrave.hexvg.api.database.DatabaseType;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class SQLiteDatabaseService implements DatabaseService {

    private final File   dbFile;
    private final Logger logger;
    private Connection   connection;

    public SQLiteDatabaseService(File dataFolder, Logger logger) {
        this.dbFile = new File(dataFolder, "hexvg.db");
        this.logger = logger;

        // Upewnij się że folder istnieje
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        init();
    }

    // ── Inicjalizacja ─────────────────────────────────────────────────────

    private void init() {
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + dbFile.getAbsolutePath()
            );

            applyPragmas();

            logger.info("[SQLite] Połączono z: " + dbFile.getName());

        } catch (ClassNotFoundException e) {
            logger.severe("[SQLite] Brak sterownika JDBC: " + e.getMessage());
            throw new RuntimeException("SQLite driver not found", e);
        } catch (SQLException e) {
            logger.severe("[SQLite] Błąd inicjalizacji: " + e.getMessage());
            throw new RuntimeException("SQLite init failed", e);
        }
    }

    /**
     * Ustawia PRAGMA dla lepszej wydajności i bezpieczeństwa.
     */
    private void applyPragmas() throws SQLException {
        try (Statement st = connection.createStatement()) {
            // WAL mode — lepsza współbieżność odczytu
            st.execute("PRAGMA journal_mode = WAL;");
            // Wymuszenie kluczy obcych
            st.execute("PRAGMA foreign_keys = ON;");
            // Synchronizacja — NORMAL = balans bezpieczeństwo/wydajność
            st.execute("PRAGMA synchronous = NORMAL;");
            // Cache — 64MB
            st.execute("PRAGMA cache_size = -65536;");
            // Przechowuj temp tabele w pamięci
            st.execute("PRAGMA temp_store = MEMORY;");
        }
    }

    // ── DatabaseService ───────────────────────────────────────────────────

    @Override
    public Connection getConnection() throws SQLException {
        // SQLite jest single-connection — sprawdzamy czy żyje
        if (connection == null || connection.isClosed()) {
            logger.warning("[SQLite] Połączenie zerwane — rekonektuję...");
            init();
        }
        return connection;
    }

    @Override
    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                // Checkpoint WAL przed zamknięciem
                try (Statement st = connection.createStatement()) {
                    st.execute("PRAGMA wal_checkpoint(TRUNCATE);");
                }
                connection.close();
                logger.info("[SQLite] Połączenie zamknięte.");
            }
        } catch (SQLException e) {
            logger.warning("[SQLite] Błąd zamykania: " + e.getMessage());
        }
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.SQLITE;
    }

    @Override
    public boolean isConnected() {
        try {
            return connection != null
                    && !connection.isClosed()
                    && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}