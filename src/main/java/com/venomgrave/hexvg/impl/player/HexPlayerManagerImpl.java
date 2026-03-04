package com.venomgrave.hexvg.impl.player;

import com.venomgrave.hexvg.api.data.PlayerDataService;
import com.venomgrave.hexvg.api.player.HexPlayer;
import com.venomgrave.hexvg.api.player.HexPlayerManager;
import com.venomgrave.hexvg.api.rank.RankHook;
import com.venomgrave.hexvg.api.session.SessionService;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class HexPlayerManagerImpl implements HexPlayerManager {

    private final SessionService    sessionService;
    private final PlayerDataService dataService;
    private final RankHook          rankHook;
    private final Logger            logger;

    // UUID → HexPlayer (tylko online gracze)
    private final Map<UUID, HexPlayer> cache = new ConcurrentHashMap<>();

    public HexPlayerManagerImpl(SessionService sessionService,
                                PlayerDataService dataService,
                                RankHook rankHook,
                                Logger logger) {
        this.sessionService = sessionService;
        this.dataService    = dataService;
        this.rankHook       = rankHook;
        this.logger         = logger;
    }

    // ── HexPlayerManager ──────────────────────────────────────────────────

    @Override
    public HexPlayer get(UUID uuid) {
        HexPlayer cached = cache.get(uuid);
        if (cached != null) return cached;

        // Fallback — gracz online ale nie w cache (nie powinno się zdarzyć)
        logger.warning("[HexPlayerManager] get() poza cache: " + uuid
                + " — tworzę tymczasowy obiekt.");
        return createPlayer(uuid, uuid.toString());
    }

    @Override
    public HexPlayer get(Player player) {
        return get(player.getUniqueId());
    }

    @Override
    public Optional<HexPlayer> findByName(String name) {
        return cache.values().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public HexPlayer getOffline(UUID uuid, String name) {
        // Nie cachujemy offline graczy
        return createPlayer(uuid, name);
    }

    @Override
    public Collection<HexPlayer> getOnlinePlayers() {
        return Collections.unmodifiableCollection(cache.values());
    }

    @Override
    public void load(UUID uuid, String name) {
        HexPlayer player = createPlayer(uuid, name);
        cache.put(uuid, player);

        if (logger != null) {
            logger.fine("[HexPlayerManager] Załadowano: " + name
                    + " (" + uuid + ")");
        }
    }

    @Override
    public void unload(UUID uuid) {
        HexPlayer removed = cache.remove(uuid);
        if (removed != null && logger != null) {
            logger.fine("[HexPlayerManager] Usunięto z cache: "
                    + removed.getName());
        }
    }

    // ── Pomocnicze ────────────────────────────────────────────────────────

    private HexPlayer createPlayer(UUID uuid, String name) {
        return new HexPlayer(
                uuid,
                name,
                sessionService,
                dataService,
                rankHook
        );
    }
}