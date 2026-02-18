package com.hexvg.core.commands;

import com.hexvg.core.HexVGCore;
import com.hexvg.core.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Bazowa klasa dla komend w HexVG pluginach.
 * Zapewnia obsługę sub-komend, uprawnień, tab-completion i wspólnych błędów.
 *
 * Przykład użycia:
 * <pre>
 *     public class ShopCommand extends BaseCommand {
 *         public ShopCommand(HexVGCore core) {
 *             super(core, "hexvg.shop.use", true); // true = wymaga gracza
 *         }
 *
 *         {@literal @}Override
 *         protected boolean execute(Player player, String[] args) {
 *             // logika komendy
 *             return true;
 *         }
 *     }
 * </pre>
 */
public abstract class BaseCommand implements CommandExecutor, TabCompleter {

    protected final HexVGCore core;
    private final String permission;
    private final boolean playerOnly;

    /**
     * @param core       instancja Core
     * @param permission wymagane uprawnienie (null = brak wymagań)
     * @param playerOnly czy komenda wymaga gracza (nie konsoli)
     */
    protected BaseCommand(HexVGCore core, String permission, boolean playerOnly) {
        this.core = core;
        this.permission = permission;
        this.playerOnly = playerOnly;
    }

    protected BaseCommand(HexVGCore core) {
        this(core, null, false);
    }

    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Sprawdź czy komenda wymaga gracza
        if (playerOnly && !(sender instanceof Player)) {
            core.getMessageManager().sendPlayerOnly(sender);
            return true;
        }

        // Sprawdź uprawnienia
        if (permission != null && !sender.hasPermission(permission)) {
            core.getMessageManager().sendNoPermission(sender);
            return true;
        }

        // Wywołaj odpowiednią metodę
        if (sender instanceof Player player) {
            return execute(player, args);
        } else {
            return executeConsole(sender, args);
        }
    }

    /**
     * Wywoływana gdy komendę wpisuje gracz.
     * Nadpisz tę metodę w swojej klasie.
     */
    protected boolean execute(Player player, String[] args) {
        return true;
    }

    /**
     * Wywoływana gdy komendę wpisuje konsola.
     * Nadpisz tę metodę jeśli komenda obsługuje konsolę.
     */
    protected boolean executeConsole(CommandSender sender, String[] args) {
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return tabComplete(sender, args);
    }

    /**
     * Tab-completion. Nadpisz tę metodę by dodać podpowiedzi.
     */
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    // ---- Pomocnicze metody dla sub-komend ----

    /**
     * Sprawdza czy args[index] pasuje do podanego tekstu (case-insensitive).
     */
    protected boolean isArg(String[] args, int index, String value) {
        return args.length > index && args[index].equalsIgnoreCase(value);
    }

    /**
     * Bezpiecznie pobiera argument z tablicy.
     */
    protected String getArg(String[] args, int index) {
        return args.length > index ? args[index] : null;
    }

    /**
     * Pobiera argument jako liczbę całkowitą.
     */
    protected Integer getArgInt(String[] args, int index) {
        String arg = getArg(args, index);
        if (arg == null) return null;
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Pobiera argument jako liczbę zmiennoprzecinkową.
     */
    protected Double getArgDouble(String[] args, int index) {
        String arg = getArg(args, index);
        if (arg == null) return null;
        try {
            return Double.parseDouble(arg);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Wysyła wiadomość "Nieprawidłowe użycie".
     */
    protected void sendUsage(CommandSender sender, String usage) {
        core.getMessageManager().send(sender, "invalid-usage",
                java.util.Map.of("{usage}", usage));
    }

    /**
     * Filtruje listę tab-completionów na podstawie wpisanego tekstu.
     */
    protected List<String> filterTabComplete(List<String> options, String typed) {
        if (typed == null || typed.isEmpty()) return options;

        List<String> filtered = new ArrayList<>();
        String lowerTyped = typed.toLowerCase();

        for (String option : options) {
            if (option.toLowerCase().startsWith(lowerTyped)) {
                filtered.add(option);
            }
        }
        return filtered;
    }

    /**
     * Wysyła wiadomość z kolorami.
     */
    protected void send(CommandSender sender, String message) {
        ChatUtils.send(sender, message);
    }
}