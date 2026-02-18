package com.hexvg.core.utils;

import com.hexvg.core.HexVGCore;
import org.bukkit.Bukkit;

/**
 * Centralny logger dla HexVG-Core i pluginów zależnych.
 * Obsługuje kolorowe wiadomości w konsoli i tryb debug.
 */
public class Logger {

    private static boolean debug = false;

    public static void setDebug(boolean debugMode) {
        debug = debugMode;
    }

    public static void info(String message) {
        log("[INFO] " + message);
    }

    public static void warning(String message) {
        log("[WARN] " + message);
    }

    public static void error(String message) {
        log("[ERROR] " + message);
    }

    public static void debug(String message) {
        if (debug) {
            log("[DEBUG] " + message);
        }
    }

    /**
     * Loguje wiadomość z prefiksem pluginu (dla pluginów zależnych).
     *
     * @param pluginName nazwa pluginu
     * @param message    wiadomość
     */
    public static void info(String pluginName, String message) {
        log("[" + pluginName + "] " + message);
    }

    public static void error(String pluginName, String message, Throwable throwable) {
        log("[" + pluginName + "] [ERROR] " + message);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    private static void log(String message) {
        // Bukkit.getConsoleSender obsługuje kolory legacy (&a, &c itp.)
        Bukkit.getConsoleSender().sendMessage(
                ChatUtils.colorize("&8[&6HexVG&8] " + message)
        );
    }
}