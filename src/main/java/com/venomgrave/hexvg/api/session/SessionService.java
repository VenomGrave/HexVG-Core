package com.venomgrave.hexvg.api.session;

import java.time.Instant;
import java.util.UUID;

public interface SessionService {

    /**
     * Rejestruje dołączenie gracza.
     * Wywoływane w PlayerJoinEvent.
     */
    void handleJoin(UUID uuid, String playerName, String ipHash);

    /**
     * Rejestruje wyjście gracza — zapisuje czas sesji do DB.
     * Wywoływane w PlayerQuitEvent.
     */
    void handleQuit(UUID uuid);

    /**
     * Zwraca czas ostatniego logowania.
     * Null jeśli gracz nigdy nie był online.
     */
    Instant getLastSeen(UUID uuid);

    /**
     * Zwraca łączny czas gry w sekundach.
     * Uwzględnia bieżącą sesję jeśli gracz jest online.
     */
    long getPlaytimeSeconds(UUID uuid);

    /**
     * Zwraca liczbę logowań.
     */
    int getLoginCount(UUID uuid);

    /**
     * Zwraca czas pierwszego dołączenia.
     * Null jeśli gracz nigdy nie był online.
     */
    Instant getFirstJoin(UUID uuid);

    /**
     * Sprawdza czy gracz kiedykolwiek był na serwerze.
     */
    boolean hasPlayed(UUID uuid);

    /**
     * Zapisuje wszystkie aktywne sesje do DB.
     * Wywoływane w onDisable() przed zamknięciem połączenia.
     */
    void flushAll();
}