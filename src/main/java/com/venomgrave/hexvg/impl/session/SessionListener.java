package com.venomgrave.hexvg.impl.session;

import com.venomgrave.hexvg.HexVGCore;
import com.venomgrave.hexvg.api.data.PlayerDataService;
import com.venomgrave.hexvg.api.event.HexPlayerJoinEvent;
import com.venomgrave.hexvg.api.event.HexPlayerQuitEvent;
import com.venomgrave.hexvg.api.player.HexPlayer;
import com.venomgrave.hexvg.api.player.HexPlayerManager;
import com.venomgrave.hexvg.api.session.SessionService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class SessionListener implements Listener {

    private final HexVGCore         core;
    private final SessionService    sessionService;
    private final PlayerDataService dataService;
    private final HexPlayerManager  playerManager;
    private final Logger            logger;

    public SessionListener(HexVGCore core,
                           SessionService sessionService,
                           PlayerDataService dataService,
                           HexPlayerManager playerManager,
                           Logger logger) {
        this.core           = core;
        this.sessionService = sessionService;
        this.dataService    = dataService;
        this.playerManager  = playerManager;
        this.logger         = logger;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var uuid   = player.getUniqueId();
        var name   = player.getName();

        InetSocketAddress address = player.getAddress();
        String ipHash = hashIp(address != null
                ? address.getAddress().getHostAddress()
                : "unknown");

        // 1. Upsert profilu gracza
        dataService.upsertProfile(uuid, name, ipHash);

        // 2. Rejestruj sesję
        sessionService.handleJoin(uuid, name, ipHash);

        // 3. Załaduj HexPlayer do cache
        playerManager.load(uuid, name);

        // 4. Sprawdź czy pierwszy join
        boolean firstJoin = sessionService.getLoginCount(uuid) == 1;

        // 5. Wywołaj HexPlayerJoinEvent
        HexPlayer hexPlayer = playerManager.get(uuid);
        core.getServer().getPluginManager()
                .callEvent(new HexPlayerJoinEvent(hexPlayer, firstJoin));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        var uuid = event.getPlayer().getUniqueId();

        // Pobierz przed unload
        HexPlayer hexPlayer = playerManager.get(uuid);
        long playtime       = hexPlayer.getPlaytimeSeconds();

        // 1. Wywołaj HexPlayerQuitEvent
        core.getServer().getPluginManager()
                .callEvent(new HexPlayerQuitEvent(hexPlayer, playtime));

        // 2. Usuń z cache
        playerManager.unload(uuid);

        // 3. Zamknij sesję
        sessionService.handleQuit(uuid);
    }

    // ── Pomocnicze ────────────────────────────────────────────────────────

    private String hashIp(String ip) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(ip.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            logger.warning("[Session] SHA-256 niedostępny: " + e.getMessage());
            return "unknown";
        }
    }
}