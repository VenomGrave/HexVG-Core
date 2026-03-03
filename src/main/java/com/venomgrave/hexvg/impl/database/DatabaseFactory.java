package com.venomgrave.hexvg.impl.database;

import com.venomgrave.hexvg.api.database.DatabaseService;
import com.venomgrave.hexvg.config.CoreConfig;

import java.io.File;
import java.util.logging.Logger;

public final class DatabaseFactory {

    private DatabaseFactory() {}

    /**
     * Tworzy odpowiednią implementację DatabaseService
     * na podstawie wartości database.type w CoreConfig.
     *
     * @param config     wczytana konfiguracja Core
     * @param dataFolder folder pluginu (dla SQLite)
     * @param logger     logger pluginu
     * @return gotowy serwis bazy danych
     * @throws IllegalArgumentException jeśli typ jest nieznany
     * @throws RuntimeException         jeśli połączenie się nie udało
     */
    public static DatabaseService create(CoreConfig config,
                                         File dataFolder,
                                         Logger logger) {
        return switch (config.getDatabaseType()) {
            case MYSQL  -> new MySQLDatabaseService(config, logger);
            case SQLITE -> new SQLiteDatabaseService(dataFolder, logger);
        };
    }
}