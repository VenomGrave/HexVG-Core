package com.hexvg.core.database;

import com.hexvg.core.HexVGCore;
import com.hexvg.core.config.ConfigManager;
import com.hexvg.core.utils.Logger;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Centralny menedżer bazy danych.
 * Obsługuje MySQL, SQLite (przez HikariCP) oraz MongoDB.
 *
 * Użycie z innych pluginów:
 * <pre>
 *     DatabaseManager db = HexVGCore.getInstance().getDatabaseManager();
 *
 *     // SQL (MySQL/SQLite)
 *     try (Connection conn = db.getConnection()) {
 *         // ... operacje na DB
 *     }
 *
 *     // MongoDB
 *     MongoDatabase mongoDB = db.getMongoDatabase();
 * </pre>
 */
public class DatabaseManager {

    private final HexVGCore plugin;

    @Getter
    private DatabaseType databaseType;

    // SQL
    private HikariDataSource hikariDataSource;

    // MongoDB
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    @Getter
    private boolean connected = false;

    public DatabaseManager(HexVGCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Nawiązuje połączenie z bazą danych na podstawie konfiguracji.
     */
    public void connect() {
        ConfigManager config = plugin.getConfigManager();
        String typeStr = config.getDatabaseType();

        try {
            databaseType = DatabaseType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            Logger.error("Nieznany typ bazy danych: " + typeStr + ". Używam SQLite.");
            databaseType = DatabaseType.SQLITE;
        }

        Logger.info("Łączenie z bazą danych typu: " + databaseType);

        switch (databaseType) {
            case MYSQL -> connectMySQL(config);
            case SQLITE -> connectSQLite(config);
            case MONGODB -> connectMongoDB(config);
        }
    }

    /**
     * Nawiązuje połączenie z MySQL przez HikariCP.
     */
    private void connectMySQL(ConfigManager config) {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getMysqlHost() + ":"
                + config.getMysqlPort() + "/" + config.getMysqlDatabase()
                + "?useSSL=false&characterEncoding=UTF-8&allowPublicKeyRetrieval=true");

        hikariConfig.setUsername(config.getMysqlUsername());
        hikariConfig.setPassword(config.getMysqlPassword());
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Pool settings
        hikariConfig.setMaximumPoolSize(config.getMysqlMaxPoolSize());
        hikariConfig.setMinimumIdle(config.getMysqlMinIdle());
        hikariConfig.setConnectionTimeout(config.getMysqlConnectionTimeout());
        hikariConfig.setIdleTimeout(config.getMysqlIdleTimeout());
        hikariConfig.setMaxLifetime(config.getMysqlMaxLifetime());
        hikariConfig.setPoolName("HexVG-MySQL-Pool");

        // Optymalizacje
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");

        try {
            hikariDataSource = new HikariDataSource(hikariConfig);
            connected = true;
            Logger.info("&aPołączono z MySQL! (" + config.getMysqlHost() + ":" + config.getMysqlPort() + ")");
        } catch (Exception e) {
            Logger.error("Nie udało się połączyć z MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Nawiązuje połączenie z SQLite przez HikariCP.
     */
    private void connectSQLite(ConfigManager config) {
        File dbFile = new File(plugin.getDataFolder(), config.getSqliteFile());

        if (!dbFile.getParentFile().exists()) {
            dbFile.getParentFile().mkdirs();
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        hikariConfig.setMaximumPoolSize(1); // SQLite nie wspiera wielu połączeń
        hikariConfig.setPoolName("HexVG-SQLite-Pool");

        // Optymalizacje SQLite
        hikariConfig.addDataSourceProperty("journal_mode", "WAL");
        hikariConfig.addDataSourceProperty("synchronous", "NORMAL");

        try {
            hikariDataSource = new HikariDataSource(hikariConfig);
            connected = true;
            Logger.info("&aPołączono z SQLite! (" + dbFile.getName() + ")");
        } catch (Exception e) {
            Logger.error("Nie udało się połączyć z SQLite: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Nawiązuje połączenie z MongoDB.
     */
    private void connectMongoDB(ConfigManager config) {
        try {
            mongoClient = MongoClients.create(config.getMongoUri());
            mongoDatabase = mongoClient.getDatabase(config.getMongoDatabase());

            // Test połączenia
            mongoDatabase.runCommand(new org.bson.Document("ping", 1));

            connected = true;
            Logger.info("&aPołączono z MongoDB! (" + config.getMongoDatabase() + ")");
        } catch (Exception e) {
            Logger.error("Nie udało się połączyć z MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Zwraca połączenie SQL z pula (MySQL lub SQLite).
     * ZAWSZE używaj try-with-resources!
     *
     * @return Connection
     * @throws SQLException gdy brak połączenia lub błąd
     */
    public Connection getConnection() throws SQLException {
        if (databaseType == DatabaseType.MONGODB) {
            throw new UnsupportedOperationException("Użyj getMongoDatabase() dla MongoDB!");
        }
        if (hikariDataSource == null || hikariDataSource.isClosed()) {
            throw new SQLException("Baza danych nie jest połączona!");
        }
        return hikariDataSource.getConnection();
    }

    /**
     * Zwraca bazę danych MongoDB.
     */
    public MongoDatabase getMongoDatabase() {
        if (databaseType != DatabaseType.MONGODB) {
            throw new UnsupportedOperationException("Użyj getConnection() dla SQL!");
        }
        return mongoDatabase;
    }

    /**
     * Sprawdza czy używamy SQL (MySQL lub SQLite).
     */
    public boolean isSQL() {
        return databaseType == DatabaseType.MYSQL || databaseType == DatabaseType.SQLITE;
    }

    /**
     * Sprawdza czy używamy MongoDB.
     */
    public boolean isMongoDB() {
        return databaseType == DatabaseType.MONGODB;
    }

    /**
     * Zamyka wszystkie połączenia z bazą danych.
     */
    public void close() {
        if (hikariDataSource != null && !hikariDataSource.isClosed()) {
            hikariDataSource.close();
            Logger.info("Połączenie SQL zamknięte.");
        }
        if (mongoClient != null) {
            mongoClient.close();
            Logger.info("Połączenie MongoDB zamknięte.");
        }
        connected = false;
    }

    /**
     * Wykonuje zapytanie SQL bezpiecznie (z obsługą błędów).
     * Dla prostych operacji bez zwracania wyników.
     */
    public void executeAsync(String sql, Object... params) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = getConnection();
                 var stmt = conn.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                stmt.executeUpdate();

            } catch (SQLException e) {
                Logger.error("Błąd wykonania zapytania SQL: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}