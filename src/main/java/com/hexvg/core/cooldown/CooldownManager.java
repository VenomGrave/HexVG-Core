package com.hexvg.core.cooldown;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Centralny menedżer cooldownów dla wszystkich pluginów HexVG.
 * Cooldowny są in-memory (resetują się po restarcie).
 * Dla persystentnych cooldownów użyj zapisu do bazy danych.
 *
 * Użycie z innych pluginów:
 * <pre>
 *     CooldownManager cooldown = HexVGCore.getInstance().getCooldownManager();
 *
 *     // Sprawdź cooldown
 *     if (cooldown.hasCooldown(player.getUniqueId(), "daily-reward")) {
 *         long remaining = cooldown.getRemaining(player.getUniqueId(), "daily-reward");
 *         // wyślij wiadomość z czasem
 *         return;
 *     }
 *
 *     // Ustaw cooldown na 24h (w sekundach)
 *     cooldown.setCooldown(player.getUniqueId(), "daily-reward", 86400);
 * </pre>
 */
public class CooldownManager {

    // Mapa: pluginKey+uuid -> czas wygaśnięcia (System.currentTimeMillis())
    private final Map<String, Long> cooldowns = new HashMap<>();

    /**
     * Ustawia cooldown dla gracza.
     *
     * @param uuid      UUID gracza
     * @param key       klucz cooldownu (np. "daily-reward", "kit-starter")
     * @param seconds   czas cooldownu w sekundach
     */
    public void setCooldown(UUID uuid, String key, long seconds) {
        String fullKey = buildKey(uuid, key);
        cooldowns.put(fullKey, System.currentTimeMillis() + (seconds * 1000));
    }

    /**
     * Sprawdza czy gracz ma aktywny cooldown.
     */
    public boolean hasCooldown(UUID uuid, String key) {
        String fullKey = buildKey(uuid, key);
        Long expiry = cooldowns.get(fullKey);

        if (expiry == null) return false;

        if (System.currentTimeMillis() >= expiry) {
            cooldowns.remove(fullKey);
            return false;
        }

        return true;
    }

    /**
     * Zwraca pozostały czas cooldownu w sekundach.
     * Zwraca 0 jeśli cooldown nie istnieje lub wygasł.
     */
    public long getRemaining(UUID uuid, String key) {
        String fullKey = buildKey(uuid, key);
        Long expiry = cooldowns.get(fullKey);

        if (expiry == null) return 0;

        long remaining = (expiry - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    /**
     * Usuwa cooldown gracza.
     */
    public void removeCooldown(UUID uuid, String key) {
        cooldowns.remove(buildKey(uuid, key));
    }

    /**
     * Usuwa wszystkie cooldowny gracza.
     */
    public void removeAllCooldowns(UUID uuid) {
        String prefix = uuid.toString() + ":";
        cooldowns.entrySet().removeIf(entry -> entry.getKey().startsWith(prefix));
    }

    /**
     * Usuwa wszystkie wygasłe cooldowny (czyszczenie pamięci).
     * Możesz wywoływać periodycznie przez scheduler.
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        cooldowns.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    /**
     * Zwraca liczbę aktywnych cooldownów.
     */
    public int size() {
        return cooldowns.size();
    }

    private String buildKey(UUID uuid, String key) {
        return uuid.toString() + ":" + key;
    }
}