package com.hexvg.core.database.migration;

import com.hexvg.core.HexVGCore;
import com.hexvg.core.database.DatabaseManager;
import com.hexvg.core.utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MigrationManager {

    private final HexVGCore plugin;
    private final DatabaseManager db;
    private final List<Migration> migrations = new ArrayList<>();

    public MigrationManager(HexVGCore plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
    }

    public void initialize() {
        if (!db.isSQL()) {
            Logger.warning("MigrationManager działa tylko z MySQL/SQLite!");
            return;
        }
        createMigrationsTable();
        registerCoreMigrations();
        runPending("HexVG-Core");
        Logger.info("MigrationManager zainicjalizowany.");
    }

    public void register(Migration migration) {
        migrations.add(migration);
    }

    public void runPending(String pluginName) {
        if (!db.isSQL()) return;

        List<Migration> pending = migrations.stream()
                .filter(m -> m.getPluginName().equals(pluginName))
                .filter(m -> !isApplied(m))
                .sorted(Comparator.comparingInt(Migration::getVersion))
                .toList();

        if (pending.isEmpty()) {
            Logger.debug("[" + pluginName + "] Brak nowych migracji.");
            return;
        }

        Logger.info("[" + pluginName + "] Wykonywanie " + pending.size() + " migracji...");
        pending.forEach(this::runMigration);
    }

    private void runMigration(Migration migration) {
        Logger.info("Migracja [" + migration.getPluginName() + "] v"
                + migration.getVersion() + " - " + migration.getName() + "...");

        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (String sql : migration.getSQL()) {
                    if (sql == null || sql.isBlank()) continue;
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.executeUpdate();
                    }
                }
                markApplied(conn, migration);
                conn.commit();
                Logger.info("&aMigracja " + migration.getName() + " wykonana pomyślnie!");
            } catch (SQLException e) {
                conn.rollback();
                Logger.error("Błąd podczas migracji " + migration.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            Logger.error("Nie można uzyskać połączenia dla migracji: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isApplied(Migration migration) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM hexvg_migrations WHERE plugin_name = ? AND version = ?")) {
            stmt.setString(1, migration.getPluginName());
            stmt.setInt(2, migration.getVersion());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Logger.error("Błąd sprawdzania migracji: " + e.getMessage());
            return false;
        }
    }

    private void markApplied(Connection conn, Migration migration) throws SQLException {
        boolean isMySQL = db.getDatabaseType().name().equals("MYSQL");
        String sql = isMySQL
                ? "INSERT INTO hexvg_migrations (plugin_name, version, name, applied_at) VALUES (?, ?, ?, NOW())"
                : "INSERT INTO hexvg_migrations (plugin_name, version, name, applied_at) VALUES (?, ?, ?, datetime('now'))";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, migration.getPluginName());
            stmt.setInt(2, migration.getVersion());
            stmt.setString(3, migration.getName());
            stmt.executeUpdate();
        }
    }

    private void createMigrationsTable() {
        boolean isMySQL = db.getDatabaseType().name().equals("MYSQL");

        // Java 17 - text blocks
        String sql = isMySQL ? """
                CREATE TABLE IF NOT EXISTS hexvg_migrations (
                    id          INT PRIMARY KEY AUTO_INCREMENT,
                    plugin_name VARCHAR(64)  NOT NULL,
                    version     INT          NOT NULL,
                    name        VARCHAR(128) NOT NULL,
                    applied_at  DATETIME     NOT NULL,
                    UNIQUE KEY unique_migration (plugin_name, version)
                )
                """ : """
                CREATE TABLE IF NOT EXISTS hexvg_migrations (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    plugin_name TEXT    NOT NULL,
                    version     INTEGER NOT NULL,
                    name        TEXT    NOT NULL,
                    applied_at  TEXT    NOT NULL,
                    UNIQUE (plugin_name, version)
                )
                """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.error("Nie udało się utworzyć tabeli migracji: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void registerCoreMigrations() {
        final boolean isMySQL = db.getDatabaseType().name().equals("MYSQL");

        register(new Migration(1, "HexVG-Core", "create_players_table") {
            @Override
            public String[] getSQL() {
                return isMySQL ? new String[]{ """
                        CREATE TABLE IF NOT EXISTS hexvg_players (
                            uuid       VARCHAR(36) PRIMARY KEY,
                            name       VARCHAR(16) NOT NULL,
                            first_join DATETIME    NOT NULL DEFAULT NOW(),
                            last_join  DATETIME    NOT NULL DEFAULT NOW()
                        )
                        """ } : new String[]{ """
                        CREATE TABLE IF NOT EXISTS hexvg_players (
                            uuid       TEXT PRIMARY KEY,
                            name       TEXT NOT NULL,
                            first_join TEXT NOT NULL DEFAULT (datetime('now')),
                            last_join  TEXT NOT NULL DEFAULT (datetime('now'))
                        )
                        """ };
            }
        });

        register(new Migration(2, "HexVG-Core", "create_cooldowns_table") {
            @Override
            public String[] getSQL() {
                return isMySQL ? new String[]{ """
                        CREATE TABLE IF NOT EXISTS hexvg_cooldowns (
                            uuid       VARCHAR(36) NOT NULL,
                            cd_key     VARCHAR(64) NOT NULL,
                            expires_at BIGINT      NOT NULL,
                            PRIMARY KEY (uuid, cd_key)
                        )
                        """ } : new String[]{ """
                        CREATE TABLE IF NOT EXISTS hexvg_cooldowns (
                            uuid       TEXT    NOT NULL,
                            cd_key     TEXT    NOT NULL,
                            expires_at INTEGER NOT NULL,
                            PRIMARY KEY (uuid, cd_key)
                        )
                        """ };
            }
        });
    }
}