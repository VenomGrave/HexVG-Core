package com.venomgrave.hexvg.api.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseService {

    /**
     * Zwraca połączenie z puli (MySQL) lub singleton (SQLite).
     * ZAWSZE używaj try-with-resources!
     */
    Connection getConnection() throws SQLException;

    /**
     * Zamyka pulę / połączenie. Wywoływane w onDisable().
     */
    void shutdown();

    /**
     * Typ aktywnej bazy — potrzebny przy dialektach SQL
     * (np. INSERT OR REPLACE vs INSERT ... ON DUPLICATE KEY UPDATE).
     */
    DatabaseType getType();

    /**
     * Sprawdza czy połączenie działa poprawnie (ping).
     */
    boolean isConnected();
}