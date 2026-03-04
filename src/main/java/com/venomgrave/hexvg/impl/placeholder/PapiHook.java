package com.venomgrave.hexvg.impl.placeholder;

import com.venomgrave.hexvg.HexVGCore;
import com.venomgrave.hexvg.api.player.HexPlayer;
import com.venomgrave.hexvg.util.TimeUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * PlaceholderAPI expansion dla HexVG-Core.
 *
 * Dostępne placeholdery:
 *   %hexvg_playtime%         — sformatowany czas gry
 *   %hexvg_playtime_seconds% — czas gry w sekundach
 *   %hexvg_login_count%      — liczba logowań
 *   %hexvg_first_join%       — data pierwszego dołączenia
 *   %hexvg_last_seen%        — data ostatniego widzenia
 *   %hexvg_nick%             — pseudonim lub nazwa gracza
 *   %hexvg_is_banned%        — true/false
 *   %hexvg_is_muted%         — true/false
 *   %hexvg_warn_count%       — liczba aktywnych warnów
 *   %hexvg_rank_weight%      — waga rangi
 */
public class PapiHook extends PlaceholderExpansion {

    private final HexVGCore core;

    public PapiHook(HexVGCore core) {
        this.core = core;
    }

    @Override
    public @NotNull String getIdentifier() { return "hexvg"; }

    @Override
    public @NotNull String getAuthor() { return "VenomGrave"; }

    @Override
    public @NotNull String getVersion() {
        return core.getDescription().getVersion();
    }

    // Nie wyrejestrowuje się przy reload pluginu
    @Override
    public boolean persist() { return true; }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        HexPlayer hp = core.getAPI().getPlayerManager().get(player);

        return switch (params.toLowerCase()) {

            case "playtime" ->
                    TimeUtil.formatPl(hp.getPlaytimeSeconds(), 2);

            case "playtime_seconds" ->
                    String.valueOf(hp.getPlaytimeSeconds());

            case "login_count" ->
                    String.valueOf(hp.getLoginCount());

            case "first_join" ->
                    hp.getFirstJoin() != null
                            ? TimeUtil.formatDate(hp.getFirstJoin())
                            : "nigdy";

            case "last_seen" ->
                    hp.getLastSeen() != null
                            ? TimeUtil.timeAgo(hp.getLastSeen())
                            : "nigdy";

            case "nick" ->
                    hp.getNick() != null
                            ? hp.getNick()
                            : player.getName();

            case "is_banned" ->
                    String.valueOf(hp.isBanned());

            case "is_muted" ->
                    String.valueOf(hp.isMuted());

            case "warn_count" ->
                    String.valueOf(hp.getActiveWarnCount());

            case "rank_weight" ->
                    String.valueOf(hp.getRankWeight());

            default -> null; // PAPI traktuje null jako nieznany placeholder
        };
    }
}