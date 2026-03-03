package com.venomgrave.hexvg.impl.database;

import com.venomgrave.hexvg.api.database.DatabaseService;
import com.venomgrave.hexvg.api.database.DatabaseType;
import com.venomgrave.hexvg.config.CoreConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class MySQLDatabaseService implements DatabaseService {

    private final Logger          logger;
    private final CoreConfig      config;
    private       HikariDataSource dataSource;

    public MySQLDatabaseService(CoreConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
        init();
    }

    // ── Inicjalizacja ─────────────────────────────────────────────────────

    private void init() {
        HikariConfig hikari = new HikariConfig();

        // JDBC URL
        hikari.setJdbcUrl(buildJdbcUrl());
        hikari.setUsername(config.getMysqlUser());
        hikari.setPassword(config.getMysqlPassword());
        hikari.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Pula połączeń
        hikari.setMaximumPoolSize(config.getMysqlPoolSize());
        hikari.setMinimumIdle(2);
        hikari.setConnectionTimeout(10_000);   // 10s na uzyskanie połączenia
        hikari.setIdleTimeout(600_000);        // 10min bezczynności
        hikari.setMaxLifetime(1_800_000);      // 30min max życie połączenia
        hikari.setKeepaliveTime(60_000);       // ping co 60s
        hikari.setPoolName("HexVG-MySQL");

        // Właściwości sterownika MySQL
        hikari.addDataSourceProperty("cachePrepStmts",          "true");
        hikari.addDataSourceProperty("prepStmtCacheSize",        "250");
        hikari.addDataSourceProperty("prepStmtCacheSqlLimit",    "2048");
        hikari.addDataSourceProperty("useServerPrepStmts",       "true");
        hikari.addDataSourceProperty("useLocalSessionState",     "true");
        hikari.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikari.addDataSourceProperty("cacheResultSetMetadata",   "true");
        hikari.addDataSourceProperty("cacheServerConfiguration", "true");
        hikari.addDataSourceProperty("elideSetAutoCommits",      "true");
        hikari.addDataSourceProperty("maintainTimeStats",        "false");
        hikari.addDataSourceProperty("characterEncoding",        "UTF-8");
        hikari.addDataSourceProperty("useUnicode",               "true");

        // Test połączenia przy starcie
        hikari.setConnectionTestQuery("SELECT 1");
        hikari.setInitializationFailTimeout(5_000);

        try {
            this.dataSource = new HikariDataSource(hikari);
            logger.info("[MySQL] Połączono z: "
                    + config.getMysqlHost() + ":" + config.getMysqlPort()
                    + "/" + config.getMysqlDatabase()
                    + " (pool: " + config.getMysqlPoolSize() + ")");
        } catch (Exception e) {
            logger.severe("[MySQL] Błąd połączenia: " + e.getMessage());
            throw new RuntimeException("MySQL init failed", e);
        }
    }

    private String buildJdbcUrl() {
        return String.format(
                "jdbc:mysql://%s:%d/%s"
                        + "?useSSL=%s"
                        + "&autoReconnect=true"
                        + "&characterEncoding=UTF-8"
                        + "&serverTimezone=UTC"
                        + "&allowPublicKeyRetrieval=true",
                config.getMysqlHost(),
                config.getMysqlPort(),
                config.getMysqlDatabase(),
                config.isMysqlSsl()
        );
    }

    // ── DatabaseService ───────────────────────────────────────────────────

    @Override
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("[MySQL] DataSource zamknięty lub niezainicjowany.");
        }
        return dataSource.getConnection();
    }

    @Override
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("[MySQL] Pula połączeń zamknięta.");
        }
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.MYSQL;
    }

    @Override
    public boolean isConnected() {
        if (dataSource == null || dataSource.isClosed()) return false;
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(2);
        } catch (SQLException e) {
            logger.warning("[MySQL] isConnected check failed: " + e.getMessage());
            return false;
        }
    }
}