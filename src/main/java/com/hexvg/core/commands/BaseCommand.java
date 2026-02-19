package com.hexvg.core.commands;

import com.hexvg.core.HexVGCore;
import com.hexvg.core.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bazowa klasa dla komend w HexVG pluginach.
 */
public abstract class BaseCommand implements CommandExecutor, TabCompleter {

    protected final HexVGCore core;
    private final String permission;
    private final boolean playerOnly;

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
        if (playerOnly && !(sender instanceof Player)) {
            core.getMessageManager().sendPlayerOnly(sender);
            return true;
        }

        if (permission != null && !sender.hasPermission(permission)) {
            core.getMessageManager().sendNoPermission(sender);
            return true;
        }

        // Java 17 - pattern matching instanceof
        if (sender instanceof Player player) {
            return execute(player, args);
        } else {
            return executeConsole(sender, args);
        }
    }

    protected boolean execute(Player player, String[] args) {
        return true;
    }

    protected boolean executeConsole(CommandSender sender, String[] args) {
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return tabComplete(sender, args);
    }

    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    protected boolean isArg(String[] args, int index, String value) {
        return args.length > index && args[index].equalsIgnoreCase(value);
    }

    protected String getArg(String[] args, int index) {
        return args.length > index ? args[index] : null;
    }

    protected Integer getArgInt(String[] args, int index) {
        String arg = getArg(args, index);
        if (arg == null) return null;
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Double getArgDouble(String[] args, int index) {
        String arg = getArg(args, index);
        if (arg == null) return null;
        try {
            return Double.parseDouble(arg);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected void sendUsage(CommandSender sender, String usage) {
        core.getMessageManager().send(sender, "invalid-usage", Map.of("{usage}", usage));
    }

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

    protected void send(CommandSender sender, String message) {
        ChatUtils.send(sender, message);
    }
}