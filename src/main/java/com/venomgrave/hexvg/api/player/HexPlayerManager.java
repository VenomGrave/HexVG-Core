package com.venomgrave.hexvg.api.player;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface HexPlayerManager {

    /**
     * Zwraca HexPlayer dla online gracza.
     * Zawsze non-null dla graczy online — obiekt tworzony przy JoinEvent.
     */
    HexPlayer get(UUID uuid);

    /**
     * Skrót dla online gracza Bukkit.
     */
    HexPlayer get(Player player);

    /**
     * Szuka gracza po nazwie (online).
     */
    Optional<HexPlayer> findByName(String name);

    /**
     * Tworzy tymczasowy HexPlayer dla offline gracza.
     * Używaj tylko gdy konieczne — odpytuje DB.
     */
    HexPlayer getOffline(UUID uuid, String name);

    /**
     * Zwraca wszystkich online graczy jako HexPlayer.
     */
    Collection<HexPlayer> getOnlinePlayers();

    /**
     * Ładuje gracza do cache przy JoinEvent.
     * Wywoływane wyłącznie przez SessionListener.
     */
    void load(UUID uuid, String name);

    /**
     * Usuwa gracza z cache przy QuitEvent.
     * Wywoływane wyłącznie przez SessionListener.
     */
    void unload(UUID uuid);
}