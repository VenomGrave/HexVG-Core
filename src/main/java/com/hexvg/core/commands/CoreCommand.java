package com.hexvg.core.commands;

import com.hexvg.core.HexVGCore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Komenda /hexcore - administracja HexVG-Core.
 *
 * Sub-komendy:
 *  /hexcore info    - informacje o pluginie
 *  /hexcore reload  - przeładowanie konfiguracji
 *  /hexcore status  - status bazy danych
 */
public class CoreCommand extends BaseCommand {

    public CoreCommand(HexVGCore core) {
        super(core, "hexvg.core.admin", false);
    }

    @Override
    protected boolean execute(Player player, String[] args) {
        return handleCommand(player, args);
    }

    @Override
    protected boolean executeConsole(CommandSender sender, String[] args) {
        return handleCommand(sender, args);
    }

    private boolean handleCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info"   -> sendInfo(sender);
            case "reload" -> handleReload(sender);
            case "status" -> handleStatus(sender);
            default       -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        send(sender, "&8&m------------------------------------");
        send(sender, "&6HexVG-Core &7- Pomoc");
        send(sender, "&8&m------------------------------------");
        send(sender, "&e/hexcore info &7- Informacje o pluginie");
        send(sender, "&e/hexcore reload &7- Przeładuj konfigurację");
        send(sender, "&e/hexcore status &7- Status bazy danych");
        send(sender, "&8&m------------------------------------");
    }

    private void sendInfo(CommandSender sender) {
        send(sender, "&8&m------------------------------------");
        send(sender, "&6HexVG-Core &7v" + core.getDescription().getVersion());
        send(sender, "&7Autorzy: &e" + String.join(", ", core.getDescription().getAuthors()));
        send(sender, "&7Baza danych: &e" + core.getDatabaseManager().getDatabaseType());
        send(sender, "&7Status DB: " + (core.getDatabaseManager().isConnected() ? "&aPołączona" : "&cRozłączona"));
        send(sender, "&7Język: &e" + core.getConfigManager().getLanguage().toUpperCase());
        send(sender, "&8&m------------------------------------");
    }

    private void handleReload(CommandSender sender) {
        core.reload();
        core.getMessageManager().send(sender, "core-reload");
    }

    private void handleStatus(CommandSender sender) {
        boolean connected = core.getDatabaseManager().isConnected();
        String type = core.getDatabaseManager().getDatabaseType().name();

        send(sender, "&8&m------------------------------------");
        send(sender, "&6Status bazy danych:");
        send(sender, "&7Typ: &e" + type);
        send(sender, "&7Połączenie: " + (connected ? "&aAktywne ✓" : "&cNieaktywne ✗"));
        send(sender, "&8&m------------------------------------");
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterTabComplete(Arrays.asList("info", "reload", "status"), args[0]);
        }
        return List.of();
    }
}