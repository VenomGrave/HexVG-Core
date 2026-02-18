package com.hexvg.core.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Narzędzia do formatowania i wysyłania wiadomości czatu.
 * Obsługuje legacy color codes (&a, &b, &c...) oraz hex kolory (&#RRGGBB).
 */
public class ChatUtils {

    /**
     * Tłumaczy legacy color codes (&) na kolory Bukkit.
     */
    public static String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Tłumaczy listę stringów na kolory.
     */
    public static List<String> colorize(List<String> messages) {
        return messages.stream()
                .map(ChatUtils::colorize)
                .collect(Collectors.toList());
    }

    /**
     * Usuwa wszystkie kody kolorów z wiadomości.
     */
    public static String strip(String message) {
        if (message == null) return "";
        return ChatColor.stripColor(colorize(message));
    }

    /**
     * Wysyła wiadomość do gracza/konsoli z kolorami.
     */
    public static void send(CommandSender sender, String message) {
        if (sender == null || message == null) return;
        sender.sendMessage(colorize(message));
    }

    /**
     * Wysyła wiadomość z podmianą placeholderów.
     * Przykład: send(player, "&aHej, {player}!", Map.of("{player}", "Steve"))
     */
    public static void send(CommandSender sender, String message, Map<String, String> placeholders) {
        sender.sendMessage(colorize(replacePlaceholders(message, placeholders)));
    }

    /**
     * Zastępuje placeholdery w wiadomości.
     */
    public static String replacePlaceholders(String message, Map<String, String> placeholders) {
        if (message == null) return "";
        String result = message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Formatuje czas (sekundy) do czytelnej formy: 1d 2h 3m 4s
     */
    public static String formatTime(long seconds) {
        if (seconds <= 0) return "0s";

        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (secs > 0 || sb.length() == 0) sb.append(secs).append("s");

        return sb.toString().trim();
    }

    /**
     * Formatuje liczby z separatorem tysięcy.
     * Przykład: 1000000 -> 1 000 000
     */
    public static String formatNumber(long number) {
        return String.format("%,d", number).replace(',', ' ');
    }

    /**
     * Formatuje liczby zmiennoprzecinkowe.
     * Przykład: 1234.5678 -> 1 234.57
     */
    public static String formatDouble(double number) {
        return String.format("%,.2f", number).replace(',', ' ');
    }

    /**
     * Wyśrodkowuje tekst w chacie Minecraft (szerokość 154 piksele).
     */
    public static String centerMessage(String message) {
        if (message == null || message.isEmpty()) return message;

        String stripped = ChatColor.stripColor(colorize(message));
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : stripped.toCharArray()) {
            if (c == '\u00A7') {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                messagePxSize += isBold ? getCharWidth(c) + 1 : getCharWidth(c);
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = 154 - halvedMessageSize;
        int spaceLength = 4; // szerokość spacji
        int compensated = 0;
        StringBuilder sb = new StringBuilder();

        while (compensated < toCompensate) {
            sb.append(' ');
            compensated += spaceLength;
        }

        return sb + colorize(message);
    }

    private static int getCharWidth(char c) {
        return switch (c) {
            case 'i', '!', ',', '.', '\'', '|' -> 2;
            case 'l' -> 3;
            case ' ', 'f', 'k', 'r', 't' -> 4;
            default -> 6;
        };
    }
}