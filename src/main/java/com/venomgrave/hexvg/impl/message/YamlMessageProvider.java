package com.venomgrave.hexvg.impl.message;

import com.venomgrave.hexvg.api.message.MessageKey;
import com.venomgrave.hexvg.api.message.MessageProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class YamlMessageProvider implements MessageProvider {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final JavaPlugin plugin;
    private final Logger     logger;
    private       String     language;

    private FileConfiguration messages;

    public YamlMessageProvider(JavaPlugin plugin, String language) {
        this.plugin   = plugin;
        this.logger   = plugin.getLogger();
        this.language = language;
        load();
    }

    // ── Ładowanie ─────────────────────────────────────────────────────────

    private void load() {
        String fileName = "messages_" + language + ".yml";
        File   file     = new File(plugin.getDataFolder(), fileName);

        // Zapisz domyślny plik z JAR jeśli nie istnieje
        if (!file.exists()) {
            if (plugin.getResource(fileName) != null) {
                plugin.saveResource(fileName, false);
            } else {
                // Fallback na polski
                logger.warning("[Messages] Brak " + fileName
                        + " — fallback na messages_pl.yml");
                language = "pl";
                fileName = "messages_pl.yml";
                file     = new File(plugin.getDataFolder(), fileName);
                if (!file.exists() && plugin.getResource(fileName) != null) {
                    plugin.saveResource(fileName, false);
                }
            }
        }

        if (file.exists()) {
            messages = YamlConfiguration.loadConfiguration(file);

            // Merge z defaults z JAR — uzupełnia brakujące klucze
            InputStream stream = plugin.getResource(fileName);
            if (stream != null) {
                FileConfiguration defaults = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(stream, StandardCharsets.UTF_8));
                messages.setDefaults(defaults);
            }

            logger.info("[Messages] Załadowano: " + fileName);
        } else {
            logger.severe("[Messages] Brak pliku językowego!"
                    + " Klucze będą wyświetlane zamiast wiadomości.");
            messages = new YamlConfiguration();
        }
    }

    // ── MessageKey API ────────────────────────────────────────────────────

    @Override
    public Component get(MessageKey key, Object... placeholders) {
        return get(key.getPath(), placeholders);
    }

    @Override
    public String getRaw(MessageKey key, Object... placeholders) {
        return getRaw(key.getPath(), placeholders);
    }

    @Override
    public void send(CommandSender sender, MessageKey key, Object... placeholders) {
        send(sender, key.getPath(), placeholders);
    }

    @Override
    public void sendToPermission(String permission,
                                 MessageKey key,
                                 Object... placeholders) {
        Component component = get(key, placeholders);

        if (permission == null || permission.isBlank()) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(component));
            Bukkit.getConsoleSender().sendMessage(component);
        } else {
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission(permission))
                    .forEach(p -> p.sendMessage(component));
        }
    }

    @Override
    public void sendTitle(Player player,
                          MessageKey titleKey,
                          MessageKey subtitleKey,
                          int fadeIn, int stay, int fadeOut,
                          Object... placeholders) {
        Component title    = get(titleKey, placeholders);
        Component subtitle = get(subtitleKey, placeholders);

        player.showTitle(net.kyori.adventure.title.Title.title(
                title,
                subtitle,
                net.kyori.adventure.title.Title.Times.times(
                        Duration.ofMillis(fadeIn  * 50L),
                        Duration.ofMillis(stay    * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                )
        ));
    }

    @Override
    public void sendActionBar(Player player,
                              MessageKey key,
                              Object... placeholders) {
        player.sendActionBar(get(key, placeholders));
    }

    @Override
    public List<Component> getList(MessageKey key, Object... placeholders) {
        return getList(key.getPath(), placeholders);
    }

    // ── String API ────────────────────────────────────────────────────────

    @Override
    public Component get(String path, Object... placeholders) {
        String raw = getRaw(path, placeholders);
        return MM.deserialize(raw, buildResolvers(placeholders));
    }

    @Override
    public String getRaw(String path, Object... placeholders) {
        String prefix = messages.getString(
                MessageKey.PREFIX.getPath(), "");
        String value  = messages.getString(path);

        if (value == null) {
            logger.warning("[Messages] Brak klucza: '" + path + "'");
            return "<red>[MISSING: " + path + "]</red>";
        }

        // Wstrzykujemy prefix i placeholdery
        value = value.replace("{prefix}", prefix);
        value = applyPlaceholders(value, placeholders);
        return value;
    }

    @Override
    public void send(CommandSender sender, String path, Object... placeholders) {
        sender.sendMessage(get(path, placeholders));
    }

    private List<Component> getList(String path, Object... placeholders) {
        List<String> lines = messages.getStringList(path);

        if (lines.isEmpty()) {
            logger.warning("[Messages] Brak listy dla: '" + path + "'");
            return List.of(MM.deserialize(
                    "<red>[MISSING LIST: " + path + "]</red>"));
        }

        String prefix = messages.getString(
                MessageKey.PREFIX.getPath(), "");
        List<Component> result = new ArrayList<>();

        for (String line : lines) {
            line = line.replace("{prefix}", prefix);
            line = applyPlaceholders(line, placeholders);
            result.add(MM.deserialize(line, buildResolvers(placeholders)));
        }
        return result;
    }

    // ── Zarządzanie ───────────────────────────────────────────────────────

    @Override
    public void reload() {
        load();
        logger.info("[Messages] Przeładowano.");
    }

    @Override
    public String getLanguage() {
        return language;
    }

    @Override
    public void setLanguage(String language) {
        this.language = language;
        load();
    }

    // ── Pomocnicze ────────────────────────────────────────────────────────

    /**
     * Buduje TagResolver z par (klucz, wartość).
     * Placeholdery w MiniMessage: <klucz>
     */
    private TagResolver buildResolvers(Object... placeholders) {
        if (placeholders == null || placeholders.length < 2) {
            return TagResolver.empty();
        }

        List<TagResolver> resolvers = new ArrayList<>();
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            String key   = String.valueOf(placeholders[i]);
            String value = String.valueOf(placeholders[i + 1]);
            resolvers.add(Placeholder.parsed(key, value));
        }
        return TagResolver.resolver(resolvers);
    }

    /**
     * Podmienia {klucz} w surowym stringu.
     */
    private String applyPlaceholders(String text, Object... placeholders) {
        if (placeholders == null || placeholders.length == 0) return text;

        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            String key   = String.valueOf(placeholders[i]);
            String value = String.valueOf(placeholders[i + 1]);
            text = text.replace("{" + key + "}", value);
        }
        return text;
    }
}