package com.hexvg.core.player;

import com.hexvg.core.HexVGCore;
import com.hexvg.core.utils.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Menedżer danych graczy - centralny profil gracza dostępny dla wszystkich pluginów.
 * Trzyma dane w pamięci podczas sesji i zapisuje/odczytuje z bazy danych.
 *
 * Użycie z innych pluginów:
 * <pre>
 *     PlayerDataManager pdm = HexVGCore.getInstance().getPlayerDataManager();
 *
 *     Optional<CorePlayerData> data = pdm.getPlayerData(player.getUniqueId());
 *     data.ifPresent(d -> {
 *         String name = d.getPlayerName();
 *     });
 * </pre>
 */
public class PlayerDataManager implements Listener {

    private final HexVGCore plugin;
    private final PlayerDataRepository repository;

    // Cache: UUID -> dane gracza
    private final Map<UUID, CorePlayerData> playerCache = new ConcurrentHashMap<>();

    public PlayerDataManager(HexVGCore plugin) {
        this.plugin = plugin;
        this.repository = new PlayerDataRepository(plugin);

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
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> loadPlayerData(player));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            savePlayerData(uuid);
            playerCache.remove(uuid);
            Logger.debug("Dane gracza " + event.getPlayer().getName() + " zapisane i usunięte z cache.");
        });
    }

    /**
     * Ładuje dane gracza z bazy danych do cache.
     * Jeśli gracz nie istnieje w DB - tworzy nowy profil.
     */
    private void loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        if (playerCache.containsKey(uuid)) {
            CorePlayerData data = playerCache.get(uuid);
            data.setPlayerName(player.getName());
            data.setLastJoin(Instant.now());
            return;
        }

        Optional<CorePlayerData> fromDb = repository.load(uuid);

        CorePlayerData data;
        if (fromDb.isPresent()) {
            data = fromDb.get();
            data.setPlayerName(player.getName());
            data.setLastJoin(Instant.now());
            repository.updateLastJoin(uuid);
            Logger.debug("Załadowano istniejące dane gracza: " + player.getName());
        } else {
            data = new CorePlayerData(uuid, player.getName());
            repository.save(data);
            Logger.debug("Utworzono nowe dane gracza: " + player.getName());
        }

        playerCache.put(uuid, data);

        // Wywołaj event na głównym wątku
        CorePlayerData finalData = data;
        plugin.getServer().getScheduler().runTask(plugin, () ->
                plugin.getServer().getPluginManager().callEvent(new CorePlayerDataLoadEvent(finalData))
        );
    }

    /**
     * Zapisuje dane gracza do bazy danych.
     */
    private void savePlayerData(UUID uuid) {
        CorePlayerData data = playerCache.get(uuid);
        if (data == null) return;
        repository.save(data);
    }

    public Optional<CorePlayerData> getPlayerData(UUID uuid) {
        return Optional.ofNullable(playerCache.get(uuid));
    }

    public CorePlayerData getPlayerDataOrNull(UUID uuid) {
        return playerCache.get(uuid);
    }

    public boolean isLoaded(UUID uuid) {
        return playerCache.containsKey(uuid);
    }

    public PlayerDataRepository getRepository() {
        return repository;
    }

    public Collection<CorePlayerData> getAllCachedPlayers() {
        return playerCache.values();
    }

    public void saveAll() {
        Logger.info("Zapisywanie danych " + playerCache.size() + " graczy...");
        playerCache.keySet().forEach(this::savePlayerData);
        Logger.info("Dane graczy zapisane.");
    }
}