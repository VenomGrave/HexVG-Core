package com.hexvg.core.messages;

import com.hexvg.core.HexVGCore;
import com.hexvg.core.utils.ChatUtils;
import com.hexvg.core.utils.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Zarządza wszystkimi wiadomościami dla HexVG-Core i pluginów zależnych.
 * Obsługuje wiele języków i podmianę placeholderów.
 */
public class MessageManager {

    private final HexVGCore plugin;
    private FileConfiguration messages;
    private String prefix;

    public MessageManager(HexVGCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Ładuje lub przeładowuje plik wiadomości.
     */
    public void load() {
        String language = plugin.getConfigManager().getLanguage();
        String filename = "messages_" + language + ".yml";

        File file = new File(plugin.getDataFolder(), filename);

        // Jeśli plik nie istnieje, kopiuj z resources
        if (!file.exists()) {
            if (plugin.getResource(filename) != null) {
                plugin.saveResource(filename, false);
            } else {
                // Fallback na angielski
                Logger.warning("Nie znaleziono pliku wiadomości dla języka: " + language + ". Używam EN.");
                plugin.saveResource("messages_en.yml", false);
                file = new File(plugin.getDataFolder(), "messages_en.yml");
            }
        }

        messages = YamlConfiguration.loadConfiguration(file);
        prefix = plugin.getConfigManager().getMessagePrefix();

        Logger.debug("Wiadomości załadowane (język: " + language + ")");
    }

    /**
     * Pobiera wiadomość po kluczu.
     */
    public String get(String key) {
        String msg = messages.getString(key);
        if (msg == null) {
            Logger.warning("Brak wiadomości dla klucza: " + key);
            return "&cBrak wiadomości: " + key;
        }
        return ChatUtils.colorize(prefix + msg);
    }

    /**
     * Pobiera wiadomość bez prefiksu.
     */
    public String getRaw(String key) {
        String msg = messages.getString(key);
        if (msg == null) {
            Logger.warning("Brak wiadomości dla klucza: " + key);
            return "&cBrak wiadomości: " + key;
        }
        return ChatUtils.colorize(msg);
    }

    /**
     * Pobiera wiadomość z podmianą placeholderów.
     * Przykład: get("cooldown", Map.of("{time}", "5s"))
     */
    public String get(String key, Map<String, String> placeholders) {
        String msg = get(key);
        return ChatUtils.replacePlaceholders(msg, placeholders);
    }

    /**
     * Wysyła wiadomość do gracza/konsoli.
     */
    public void send(CommandSender sender, String key) {
        sender.sendMessage(get(key));
    }

    /**
     * Wysyła wiadomość z placeholderami.
     */
    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(get(key, placeholders));
    }

    /**
     * Wysyła wiadomość "no-permission".
     */
    public void sendNoPermission(CommandSender sender) {
        send(sender, "no-permission");
    }

    /**
     * Wysyła wiadomość "player-only".
     */
    public void sendPlayerOnly(CommandSender sender) {
        send(sender, "player-only");
    }

    /**
     * Wysyła wiadomość cooldown z podmianym czasem.
     */
    public void sendCooldown(CommandSender sender, long remainingSeconds) {
        send(sender, "cooldown", Map.of("{time}", ChatUtils.formatTime(remainingSeconds)));
    }

    /**
     * Wysyła wiadomość błędu bazy danych.
     */
    public void sendDatabaseError(CommandSender sender) {
        send(sender, "database-error");
    }

    /**
     * Sprawdza czy klucz istnieje.
     */
    public boolean has(String key) {
        return messages.contains(key);
    }

    /**
     * Zwraca surowy obiekt FileConfiguration wiadomości.
     */
    public FileConfiguration getRawConfig() {
        return messages;
    }

    /**
     * Zwraca aktualny prefiks.
     */
    public String getPrefix() {
        return ChatUtils.colorize(prefix);
    }
}