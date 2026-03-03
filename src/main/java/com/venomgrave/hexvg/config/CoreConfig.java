package com.venomgrave.hexvg.config;

import com.venomgrave.hexvg.api.database.DatabaseType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class CoreConfig {

    private final Logger logger;

    // Database
    private DatabaseType databaseType;
    private String  mysqlHost;
    private int     mysqlPort;
    private String  mysqlDatabase;
    private String  mysqlUser;
    private String  mysqlPassword;
    private boolean mysqlSsl;
    private int     mysqlPoolSize;

    // General
    private String  defaultLanguage;
    private boolean debug;

    public CoreConfig(JavaPlugin plugin) {
        this.logger = plugin.getLogger();
    }

    /**
     * Wczytuje i waliduje config.yml.
     * Zwraca false jeśli znaleziono błąd krytyczny.
     */
    public boolean load(FileConfiguration cfg) {
        boolean valid = true;

        // ── Database ──────────────────────────────────────────────────────
        String dbTypeRaw = cfg.getString("database.type", "SQLITE").toUpperCase();
        try {
            databaseType = DatabaseType.valueOf(dbTypeRaw);
        } catch (IllegalArgumentException e) {
            logger.severe("[CoreConfig] Nieznany typ bazy: '" + dbTypeRaw
                    + "'. Dozwolone: MYSQL, SQLITE.");
            valid = false;
        }

        if (databaseType == DatabaseType.MYSQL) {
            mysqlHost = cfg.getString("database.mysql.host", "");
            if (mysqlHost.isBlank()) {
                logger.severe("[CoreConfig] database.mysql.host jest puste!");
                valid = false;
            }

            mysqlPort = cfg.getInt("database.mysql.port", 3306);
            if (mysqlPort < 1 || mysqlPort > 65535) {
                logger.severe("[CoreConfig] database.mysql.port poza zakresem: " + mysqlPort);
                valid = false;
            }

            mysqlDatabase = cfg.getString("database.mysql.database", "");
            if (mysqlDatabase.isBlank()) {
                logger.severe("[CoreConfig] database.mysql.database jest puste!");
                valid = false;
            }

            mysqlUser     = cfg.getString("database.mysql.user",     "root");
            mysqlPassword = cfg.getString("database.mysql.password", "");
            mysqlSsl      = cfg.getBoolean("database.mysql.ssl",     false);

            mysqlPoolSize = cfg.getInt("database.mysql.pool-size", 10);
            if (mysqlPoolSize < 1 || mysqlPoolSize > 50) {
                logger.warning("[CoreConfig] pool-size poza zakresem (1-50). Ustawiam 10.");
                mysqlPoolSize = 10;
            }
        }

        // ── General ───────────────────────────────────────────────────────
        defaultLanguage = cfg.getString("general.language", "pl").toLowerCase();
        if (!defaultLanguage.equals("pl") && !defaultLanguage.equals("en")) {
            logger.warning("[CoreConfig] Nieznany język: '"
                    + defaultLanguage + "'. Ustawiam pl.");
            defaultLanguage = "pl";
        }

        debug = cfg.getBoolean("general.debug", false);
        if (debug) logger.info("[CoreConfig] Tryb DEBUG włączony.");

        return valid;
    }

    // ── Gettery ───────────────────────────────────────────────────────────
    public DatabaseType getDatabaseType()  { return databaseType; }
    public String  getMysqlHost()          { return mysqlHost; }
    public int     getMysqlPort()          { return mysqlPort; }
    public String  getMysqlDatabase()      { return mysqlDatabase; }
    public String  getMysqlUser()          { return mysqlUser; }
    public String  getMysqlPassword()      { return mysqlPassword; }
    public boolean isMysqlSsl()            { return mysqlSsl; }
    public int     getMysqlPoolSize()      { return mysqlPoolSize; }
    public String  getDefaultLanguage()    { return defaultLanguage; }
    public boolean isDebug()               { return debug; }
}