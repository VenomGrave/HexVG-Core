package com.venomgrave.hexvg.api.player;

import com.venomgrave.hexvg.api.session.SessionService;
import com.venomgrave.hexvg.api.data.PlayerDataService;
import com.venomgrave.hexvg.api.rank.RankHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.UUID;

/**
 * Centralny obiekt gracza w ekosystemie HexVG.
 *
 * Zamiast odpytywać każdy serwis osobno:
 *   core.getSessionService().getPlaytimeSeconds(uuid)
 *   core.getPlayerDataService().isBanned(uuid)
 *
 * Wystarczy:
 *   HexPlayer hp = core.getAPI().getPlayerManager().get(uuid);
 *   hp.getPlaytimeSeconds();
 *   hp.isBanned();
 */
public class HexPlayer {

    private final UUID               uuid;
    private final String             name;
    private final SessionService     sessionService;
    private final PlayerDataService  dataService;
    private final RankHook           rankHook;

    // Cache danych które nie zmieniają się w trakcie sesji
    private final Instant firstJoin;
    private final int     loginCount;

    // Dane które mogą się zmieniać — odczytywane na żywo
    // (nick, ban status, playtime)

    public HexPlayer(UUID uuid,
                     String name,
                     SessionService sessionService,
                     PlayerDataService dataService,
                     RankHook rankHook) {
        this.uuid           = uuid;
        this.name           = name;
        this.sessionService = sessionService;
        this.dataService    = dataService;
        this.rankHook       = rankHook;

        // Cache przy tworzeniu obiektu
        this.firstJoin  = sessionService.getFirstJoin(uuid);
        this.loginCount = sessionService.getLoginCount(uuid);
    }

    // ── Identyfikacja ─────────────────────────────────────────────────────

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    /**
     * Zwraca aktywnego gracza Bukkit lub null jeśli offline.
     */
    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean isOnline() {
        return Bukkit.getPlayer(uuid) != null;
    }

    // ── Sesja ─────────────────────────────────────────────────────────────

    /**
     * Łączny czas gry w sekundach.
     * Uwzględnia bieżącą sesję jeśli gracz jest online.
     */
    public long getPlaytimeSeconds() {
        return sessionService.getPlaytimeSeconds(uuid);
    }

    /**
     * Czas ostatniego logowania.
     */
    public Instant getLastSeen() {
        return sessionService.getLastSeen(uuid);
    }

    /**
     * Czas pierwszego dołączenia (cache — nie odpytuje DB).
     */
    public Instant getFirstJoin() {
        return firstJoin;
    }

    /**
     * Liczba logowań (cache — nie odpytuje DB).
     */
    public int getLoginCount() {
        return loginCount;
    }

    /**
     * Sprawdza czy gracz kiedykolwiek był na serwerze.
     */
    public boolean hasPlayed() {
        return loginCount > 0;
    }

    // ── Profil ────────────────────────────────────────────────────────────

    /**
     * Aktualny pseudonim (lub null jeśli brak).
     */
    public String getNick() {
        return dataService.getNick(uuid);
    }

    /**
     * Hash pierwszego IP gracza.
     */
    public String getFirstIpHash() {
        return dataService.getFirstIpHash(uuid);
    }

    /**
     * Sprawdza czy gracz jest zbanowany.
     * (Szybkie sprawdzenie z cache PlayerDataService)
     */
    public boolean isBanned() {
        return dataService.isBanned(uuid);
    }

    /**
     * Sprawdza czy gracz jest wyciszony.
     */
    public boolean isMuted() {
        return dataService.isMuted(uuid);
    }

    /**
     * Liczba aktywnych warnów.
     */
    public int getActiveWarnCount() {
        return dataService.getActiveWarnCount(uuid);
    }

    // ── Ranga ─────────────────────────────────────────────────────────────

    /**
     * Waga rangi gracza z LuckPerms.
     * 0 jeśli brak RankHook lub brak meta.weight.
     */
    public int getRankWeight() {
        if (rankHook == null) return 0;
        Player player = getBukkitPlayer();
        if (player == null) return 0;
        return rankHook.getWeight(player);
    }

    /**
     * Sprawdza czy gracz ma bypass dla danej operacji.
     */
    public boolean hasBypass(String operation) {
        if (rankHook == null) {
            Player player = getBukkitPlayer();
            return player != null && player.isOp();
        }
        Player player = getBukkitPlayer();
        if (player == null) return false;
        return rankHook.hasBypass(player, operation);
    }

    // ── Pomocnicze ────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "HexPlayer{uuid=" + uuid + ", name='" + name + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HexPlayer other)) return false;
        return uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}