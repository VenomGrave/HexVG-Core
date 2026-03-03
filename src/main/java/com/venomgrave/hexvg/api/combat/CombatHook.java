package com.venomgrave.hexvg.api.combat;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Hook do systemu walki.
 * Implementacja dostarczana przez zewnętrzny plugin PvP
 * poprzez HexVGCore.getInstance().registerCombatHook(hook).
 */
public interface CombatHook {

    /**
     * Sprawdza czy gracz jest w walce.
     */
    boolean isInCombat(Player player);

    /**
     * Sprawdza po UUID (gdy gracz może być offline).
     */
    boolean isInCombat(UUID uuid);

    /**
     * Ile sekund pozostało do końca walki.
     * 0 jeśli gracz nie jest w walce.
     */
    int getRemainingCombatSeconds(Player player);
}