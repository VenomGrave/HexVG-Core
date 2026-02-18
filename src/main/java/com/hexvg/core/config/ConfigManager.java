package com.hexvg.core.config;

import com.hexvg.core.HexVGCore;
import com.hexvg.core.utils.Logger;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Zarządza konfiguracją HexVG-Core.
 * Udostępnia metody do odczytu ustawień i bazową klasę dla konfiguracji w innych pluginach.
 */
public class ConfigManager {

    private final HexVGCore plugin;
    private FileConfiguration config;

    // Cached wartości konfiguracji
    @Getter private String databaseType;
    @Getter private boolean debug;
    @Getter private String messagePrefix;
    @Getter private String language;

    // MySQL
    @Getter private String mysqlHost;
    @Getter private int mysqlPort;
    @Getter private String mysqlDatabase;
    @Getter private String mysqlUsername;
    @Getter private String mysqlPassword;
    @Getter private int mysqlMaxPoolSize;
    @Getter private int mysqlMinIdle;
    @Getter private long mysqlConnectionTimeout;
    @Getter private long mysqlIdleTimeout;
    @Getter private long mysqlMaxLifetime;

    // SQLite
    @Getter private String sqliteFile;

    // MongoDB
    @Getter private String mongoUri;
    @Getter private String mongoDatabase;

    public ConfigManager(HexVGCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Ładuje lub przeładowuje konfigurację z pliku.
     */
    public void load() {
        // Skopiuj domyślny config jeśli nie istnieje
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Załaduj wartości do pól
        cacheValues();

        Logger.debug("Konfiguracja załadowana.");
    }

    /**
     * Zapisuje aktualne wartości konfiguracji do pól (cache).
     */
    private void cacheValues() {
        databaseType = config.getString("database.type", "SQLITE").toUpperCase();
        debug = config.getBoolean("logging.debug", false);
        messagePrefix = config.getString("messages.prefix", "&8[&6HexVG&8] ");
        language = config.getString("messages.language", "pl");

        // MySQL
        mysqlHost = config.getString("database.mysql.host", "localhost");
        mysqlPort = config.getInt("database.mysql.port", 3306);
        mysqlDatabase = config.getString("database.mysql.database", "hexvg");
        mysqlUsername = config.getString("database.mysql.username", "root");
        mysqlPassword = config.getString("database.mysql.password", "");
        mysqlMaxPoolSize = config.getInt("database.mysql.pool.maximum-pool-size", 10);
        mysqlMinIdle = config.getInt("database.mysql.pool.minimum-idle", 2);
        mysqlConnectionTimeout = config.getLong("database.mysql.pool.connection-timeout", 30000);
        mysqlIdleTimeout = config.getLong("database.mysql.pool.idle-timeout", 600000);
        mysqlMaxLifetime = config.getLong("database.mysql.pool.max-lifetime", 1800000);

        // SQLite
        sqliteFile = config.getString("database.sqlite.file", "hexvg.db");

        // MongoDB
        mongoUri = config.getString("database.mongodb.uri", "mongodb://localhost:27017");
        mongoDatabase = config.getString("database.mongodb.database", "hexvg");
    }

    /**
     * Zwraca surowy obiekt FileConfiguration.
     */
    public FileConfiguration getRaw() {
        return config;
    }

    // ---- Bazowa klasa dla konfiguracji innych pluginów ----

    /**
     * Tworzy i ładuje plik konfiguracyjny dla danego pluginu.
     * Użycie w innych pluginach:
     * <pre>
     *     FileConfiguration myConfig = ConfigManager.loadConfig(myPlugin, "config.yml");
     * </pre>
     */
    public static FileConfiguration loadConfig(org.bukkit.plugin.java.JavaPlugin plugin, String filename) {
        File file = new File(plugin.getDataFolder(), filename);

        if (!file.exists()) {
            plugin.saveResource(filename, false);
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Zapisuje konfigurację do pliku.
     */
    public static void saveConfig(org.bukkit.plugin.java.JavaPlugin plugin,
                                  FileConfiguration config,
                                  String filename) {
        File file = new File(plugin.getDataFolder(), filename);
        try {
            config.save(file);
        } catch (IOException e) {
            Logger.error(plugin.getName(), "Nie udało się zapisać pliku " + filename, e);
        }
    }
}