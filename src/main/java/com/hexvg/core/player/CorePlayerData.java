package com.hexvg.core.player;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Centralny profil gracza.
 * Przechowuje podstawowe dane gracza wspólne dla wszystkich pluginów.
 * Pluginy mogą dodawać własne dane przez system metadanych (customData).
 */
@Getter
public class CorePlayerData {

    private final UUID uuid;

    @Setter
    private String playerName;

    private final Instant firstJoin;

    @Setter
    private Instant lastJoin;

    // System metadanych dla pluginów zależnych
    // Klucz: "pluginName:klucz", Wartość: dowolny obiekt
    private final Map<String, Object> customData = new HashMap<>();

    public CorePlayerData(UUID uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.firstJoin = Instant.now();
        this.lastJoin = Instant.now();
    }

    // ---- Custom data API dla innych pluginów ----

    /**
     * Zapisuje wartość dla danego klucza.
     * Konwencja klucza: "nazwaPluginu:klucz" np. "HexShop:balance"
     */
    public void set(String key, Object value) {
        customData.put(key, value);
    }

    /**
     * Pobiera wartość dla danego klucza.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) customData.get(key);
    }

    /**
     * Pobiera wartość z domyślną wartością.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        Object value = customData.get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * Sprawdza czy klucz istnieje.
     */
    public boolean has(String key) {
        return customData.containsKey(key);
    }

    /**
     * Usuwa klucz.
     */
    public void remove(String key) {
        customData.remove(key);
    }

    /**
     * Usuwa wszystkie dane dla danego pluginu.
     * Użycie: removePlugin("HexShop")
     */
    public void removePlugin(String pluginName) {
        String prefix = pluginName + ":";
        customData.entrySet().removeIf(e -> e.getKey().startsWith(prefix));
    }

    @Override
    public String toString() {
        return "CorePlayerData{uuid=" + uuid + ", name=" + playerName + "}";
    }
}