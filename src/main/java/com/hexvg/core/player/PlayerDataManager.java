package com.hexvg.core.player;

import com.hexvg.core.HexVGCore;
import com.hexvg.core.utils.Logger;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Menedżer danych graczy - centralny profil gracza dostępny dla wszystkich pluginów.
 * Trzyma dane w pamięci podczas sesji i umożliwia zapis/odczyt z bazy danych.
 *
 * Użycie z innych pluginów:
 * <pre>
 *     PlayerDataManager pdm = HexVGCore.getInstance().getPlayerDataManager();
 *
 *     // Pobierz profil gracza
 *     Optional<CorePlayerData> data = pdm.getPlayerData(player.getUniqueId());
 *     data.ifPresent(d -> {
 *         String name = d.getPlayerName();
 *         // ...
 *     });
 * </pre>
 */
public class PlayerDataManager implements Listener {

    private final HexVGCore plugin;

    // Cache: UUID -> dane gracza
    private final Map<UUID, CorePlayerData> playerCache = new ConcurrentHashMap<>();

    public PlayerDataManager(HexVGCore plugin) {
        this.plugin = plugin;

        // Rejestracja event listenerów
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Załaduj dane graczy online (ważne przy /reload)
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            loadPlayerData(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Ładowanie async by nie blokować głównego wątku
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            loadPlayerData(player);
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        // Zapisz dane przed usunięciem z cache
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            savePlayerData(uuid);
            playerCache.remove(uuid);
            Logger.debug("Dane gracza " + event.getPlayer().getName() + " zapisane i usunięte z cache.");
        });
    }

    /**
     * Ładuje dane gracza z bazy danych do cache.
     */
    private void loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();

        if (playerCache.containsKey(uuid)) return;

        // Tutaj możesz dodać ładowanie z DB
        // Na razie tworzymy nowy profil
        CorePlayerData data = new CorePlayerData(uuid, player.getName());
        playerCache.put(uuid, data);

        Logger.debug("Załadowano dane gracza: " + player.getName());
    }

    /**
     * Zapisuje dane gracza do bazy danych.
     */
    private void savePlayerData(UUID uuid) {
        CorePlayerData data = playerCache.get(uuid);
        if (data == null) return;

        // Tutaj możesz dodać zapis do DB
        Logger.debug("Zapisano dane gracza: " + data.getPlayerName());
    }

    /**
     * Zwraca dane gracza z cache.
     */
    public Optional<CorePlayerData> getPlayerData(UUID uuid) {
        return Optional.ofNullable(playerCache.get(uuid));
    }

    /**
     * Zwraca dane gracza bezpośrednio (może być null).
     */
    public CorePlayerData getPlayerDataOrNull(UUID uuid) {
        return playerCache.get(uuid);
    }

    /**
     * Sprawdza czy dane gracza są załadowane.
     */
    public boolean isLoaded(UUID uuid) {
        return playerCache.containsKey(uuid);
    }

    /**
     * Zwraca wszystkich graczy w cache.
     */
    public Collection<CorePlayerData> getAllCachedPlayers() {
        return playerCache.values();
    }

    /**
     * Zapisuje dane wszystkich graczy (np. przy wyłączaniu serwera).
     */
    public void saveAll() {
        Logger.info("Zapisywanie danych " + playerCache.size() + " graczy...");
        playerCache.keySet().forEach(this::savePlayerData);
        Logger.info("Dane graczy zapisane.");
    }
}