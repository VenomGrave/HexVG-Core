package com.venomgrave.hexvg.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public final class TextUtil {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();

    private TextUtil() {}

    // ── Parsowanie ────────────────────────────────────────────────────────

    /**
     * Parsuje MiniMessage → Component.
     * Przykład: "<green>Witaj <yellow>{name}</yellow>"
     */
    public static Component parse(String miniMessage) {
        if (miniMessage == null || miniMessage.isBlank()) {
            return Component.empty();
        }
        return MM.deserialize(miniMessage);
    }

    /**
     * Parsuje legacy (&a, &b, &#RRGGBB) → Component.
     */
    public static Component parseLegacy(String legacy) {
        if (legacy == null || legacy.isBlank()) {
            return Component.empty();
        }
        return LEGACY.deserialize(legacy);
    }

    /**
     * Konwertuje legacy string → MiniMessage string.
     * Przydatne przy migracji starych configów.
     */
    public static String legacyToMiniMessage(String legacy) {
        if (legacy == null) return "";
        Component component = LEGACY.deserialize(legacy);
        return MM.serialize(component);
    }

    /**
     * Konwertuje Component → plain text (bez formatowania).
     */
    public static String toPlain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    /**
     * Konwertuje Component → legacy string (&a, &b itd.).
     * Przydatne dla API które nie wspierają Adventure.
     */
    public static String toLegacy(Component component) {
        return LEGACY.serialize(component);
    }

    /**
     * Konwertuje Component → MiniMessage string.
     */
    public static String toMiniMessage(Component component) {
        return MM.serialize(component);
    }

    // ── Podmienianie placeholderów ────────────────────────────────────────

    /**
     * Podmienia placeholdery w stylu {klucz} w surowym stringu.
     * Przyjmuje pary: klucz, wartość, klucz, wartość...
     *
     * Przykład: replace("Hej {player}!", "player", "Steve")
     */
    public static String replace(String text, Object... pairs) {
        if (text == null || pairs == null || pairs.length == 0) {
            return text == null ? "" : text;
        }
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "Pary muszą być parzyste: klucz, wartość, klucz, wartość...");
        }

        for (int i = 0; i < pairs.length; i += 2) {
            String key   = String.valueOf(pairs[i]);
            String value = String.valueOf(pairs[i + 1]);
            text = text.replace("{" + key + "}", value);
        }
        return text;
    }

    /**
     * Sprawdza czy string zawiera tagi MiniMessage.
     */
    public static boolean hasMiniMessageTags(String text) {
        if (text == null) return false;
        return text.contains("<") && text.contains(">");
    }

    /**
     * Usuwa wszystkie tagi MiniMessage ze stringa.
     */
    public static String stripMiniMessage(String text) {
        if (text == null) return "";
        return toPlain(parse(text));
    }

    /**
     * Centruje tekst w oknie czatu (domyślna szerokość: 80 znaków).
     */
    public static String center(String text, int lineWidth) {
        if (text == null || text.isBlank()) return text;
        String plain = stripMiniMessage(text);
        int spaces = Math.max(0, (lineWidth - plain.length()) / 2);
        return " ".repeat(spaces) + text;
    }

    public static String center(String text) {
        return center(text, 80);
    }
}