package com.hexvg.core.integrations;

import com.hexvg.core.HexVGCore;
import com.hexvg.core.utils.Logger;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceholderAPIIntegration {

    private final HexVGCore core;
    private boolean available = false;

    public PlaceholderAPIIntegration(HexVGCore core) {
        this.core = core;
        if (core.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            available = true;
            registerCoreExpansion();
            Logger.info("PlaceholderAPI znaleziony - integracja aktywna.");
        } else {
            Logger.warning("PlaceholderAPI nie znaleziony. Placeholdery nie będą działać.");
        }
    }

    public boolean isAvailable() { return available; }

    public String parse(Player player, String text) {
        if (!available || text == null) return text;
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public String parse(String text) {
        if (!available || text == null) return text;
        return PlaceholderAPI.setPlaceholders(null, text);
    }

    public boolean register(PlaceholderExpansion expansion) {
        if (!available) return false;
        boolean result = expansion.register();
        if (result) Logger.info("Zarejestrowano ekspansję PAPI: %" + expansion.getIdentifier() + "_..%");
        else Logger.warning("Nie udało się zarejestrować ekspansji PAPI: " + expansion.getIdentifier());
        return result;
    }

    private void registerCoreExpansion() {
        new PlaceholderExpansion() {
            @Override public String getIdentifier() { return "hexvg"; }
            @Override public String getAuthor()     { return "HexVG"; }
            @Override public String getVersion()    { return core.getDescription().getVersion(); }
            @Override public boolean persist()      { return true; }

            @Override
            public String onPlaceholderRequest(Player player, String identifier) {
                if (player == null) return "";
                // Java 17 - switch expression
                return switch (identifier) {
                    case "player_name"        -> player.getName();
                    case "player_uuid"        -> player.getUniqueId().toString();
                    case "player_displayname" -> player.getDisplayName();
                    case "server_version"     -> core.getDescription().getVersion();
                    case "player_ping"        -> String.valueOf(player.getPing());
                    case "player_world"       -> player.getWorld().getName();
                    default                   -> null;
                };
            }
        }.register();
    }
}