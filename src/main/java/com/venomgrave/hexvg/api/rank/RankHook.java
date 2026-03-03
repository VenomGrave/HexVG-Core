package com.venomgrave.hexvg.api.rank;

import org.bukkit.entity.Player;

public interface RankHook {

    /**
     * Zwraca wagę rangi gracza (meta.weight z LuckPerms).
     * Wyższy weight = wyższa ranga.
     * Domyślnie 0 jeśli brak meta.
     */
    int getWeight(Player player);

    /**
     * Sprawdza czy gracz ma bypass dla danej operacji.
     * Operacje: "ban", "mute", "kick", "freeze"
     */
    boolean hasBypass(Player player, String operation);

    /**
     * Sprawdza czy staff może wykonać akcję na target.
     * Staff nie może karać graczy z wyższą lub równą wagą rangi.
     */
    default boolean canPunish(Player staff, Player target) {
        return getWeight(staff) > getWeight(target);
    }
}