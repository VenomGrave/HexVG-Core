package com.venomgrave.hexvg.api.message;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public interface MessageProvider {

    // ── MessageKey API — używaj w kodzie ──────────────────────────────────

    Component get(MessageKey key, Object... placeholders);

    String getRaw(MessageKey key, Object... placeholders);

    void send(CommandSender sender, MessageKey key, Object... placeholders);

    void sendToPermission(String permission, MessageKey key, Object... placeholders);

    void sendTitle(Player player,
                   MessageKey titleKey,
                   MessageKey subtitleKey,
                   int fadeIn, int stay, int fadeOut,
                   Object... placeholders);

    void sendActionBar(Player player, MessageKey key, Object... placeholders);

    List<Component> getList(MessageKey key, Object... placeholders);

    // ── String API — gdy klucz pochodzi z zewnątrz ────────────────────────

    Component get(String path, Object... placeholders);

    String getRaw(String path, Object... placeholders);

    void send(CommandSender sender, String path, Object... placeholders);

    // ── Zarządzanie ───────────────────────────────────────────────────────

    void reload();

    String getLanguage();

    void setLanguage(String language);
}