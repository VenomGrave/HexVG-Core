package com.venomgrave.hexvg.impl.session;

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

    private final SessionService sessionService;
    private final Logger         logger;

    public SessionListener(SessionService sessionService, Logger logger) {
        this.sessionService = sessionService;
        this.logger         = logger;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        InetSocketAddress address = player.getAddress();
        String rawIp = address != null
                ? address.getAddress().getHostAddress()
                : "unknown";

        // SHA-256 hash IP — zgodność z RODO
        String ipHash = hashIp(rawIp);

        sessionService.handleJoin(
                player.getUniqueId(),
                player.getName(),
                ipHash
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        sessionService.handleQuit(event.getPlayer().getUniqueId());
    }

    // ── Pomocnicze ────────────────────────────────────────────────────────

    /**
     * Hashuje IP algorytmem SHA-256 i zwraca pierwsze 16 znaków.
     * Nie przechowujemy surowego IP — zgodność z RODO.
     */
    private String hashIp(String ip) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(ip.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            // 16 znaków = 64 bity = wystarczające do identyfikacji sesji
            return sb.substring(0, 16);

        } catch (NoSuchAlgorithmException e) {
            logger.warning("[Session] SHA-256 niedostępny: " + e.getMessage());
            return "unknown";
        }
    }
}