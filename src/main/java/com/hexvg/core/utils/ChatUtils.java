package com.hexvg.core.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatUtils {

    public static String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> colorize(List<String> messages) {
        List<String> result = new ArrayList<>();
        for (String msg : messages) result.add(colorize(msg));
        return result;
    }

    public static String strip(String message) {
        if (message == null) return "";
        return ChatColor.stripColor(colorize(message));
    }

    public static void send(CommandSender sender, String message) {
        if (sender == null || message == null) return;
        sender.sendMessage(colorize(message));
    }

    public static void send(CommandSender sender, String message, Map<String, String> placeholders) {
        sender.sendMessage(colorize(replacePlaceholders(message, placeholders)));
    }

    public static String replacePlaceholders(String message, Map<String, String> placeholders) {
        if (message == null) return "";
        String result = message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

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

    public static String formatNumber(long number) {
        return String.format("%,d", number).replace(',', ' ');
    }

    public static String formatDouble(double number) {
        return String.format("%,.2f", number).replace(',', ' ');
    }

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

        int toCompensate = 154 - messagePxSize / 2;
        StringBuilder sb = new StringBuilder();
        int compensated = 0;
        while (compensated < toCompensate) { sb.append(' '); compensated += 4; }
        return sb + colorize(message);
    }

    private static int getCharWidth(char c) {
        // Java 17 - switch expression z wieloma labelami
        return switch (c) {
            case 'i', '!', ',', '.', '\'', '|' -> 2;
            case 'l'                            -> 3;
            case ' ', 'f', 'k', 'r', 't'       -> 4;
            default                             -> 6;
        };
    }
}