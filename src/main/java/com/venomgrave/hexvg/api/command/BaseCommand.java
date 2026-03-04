package com.venomgrave.hexvg.api.command;

import com.venomgrave.hexvg.HexVGCore;
import com.venomgrave.hexvg.api.message.MessageKey;
import com.venomgrave.hexvg.api.message.MessageProvider;
import com.venomgrave.hexvg.api.player.HexPlayer;
import com.venomgrave.hexvg.api.player.HexPlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Abstrakcyjna klasa bazowa dla wszystkich komend HexVG.
 *
 * Każda komenda:
 * - ma dostęp do MessageProvider bez boilerplate
 * - automatycznie sprawdza player-only i permisje
 * - ma skrót do HexPlayer
 */
public abstract class BaseCommand implements CommandExecutor, TabCompleter {

    protected final HexVGCore        core;
    protected final MessageProvider  messages;
    protected final HexPlayerManager playerManager;

    // Opcjonalne — ustawiaj w konstruktorze podklasy
    protected String  permission   = null;
    protected boolean playerOnly   = false;

    protected BaseCommand(HexVGCore core) {
        this.core          = core;
        this.messages      = core.getAPI().getMessageProvider();
        this.playerManager = core.getAPI().getPlayerManager();
    }

    // ── CommandExecutor ───────────────────────────────────────────────────

    @Override
    public final boolean onCommand(CommandSender sender,
                                   Command command,
                                   String label,
                                   String[] args) {
        // Sprawdź player-only
        if (playerOnly && !(sender instanceof Player)) {
            messages.send(sender, MessageKey.PLAYER_ONLY);
            return true;
        }

        // Sprawdź permisję
        if (permission != null && !sender.hasPermission(permission)) {
            messages.send(sender, MessageKey.NO_PERMISSION);
            return true;
        }

        // Deleguj do implementacji
        execute(sender, command, label, args);
        return true;
    }

    /**
     * Implementacja komendy — nie sprawdzaj tu permisji ani player-only,
     * to już zostało zrobione wyżej.
     */
    protected abstract void execute(CommandSender sender,
                                    Command command,
                                    String label,
                                    String[] args);

    // ── TabCompleter ──────────────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender,
                                      Command command,
                                      String label,
                                      String[] args) {
        if (permission != null && !sender.hasPermission(permission)) {
            return Collections.emptyList();
        }
        return tabComplete(sender, command, label, args);
    }

    /**
     * Opcjonalne tab-complete — nadpisz w podklasie.
     */
    protected List<String> tabComplete(CommandSender sender,
                                       Command command,
                                       String label,
                                       String[] args) {
        return Collections.emptyList();
    }

    // ── Skróty ────────────────────────────────────────────────────────────

    /**
     * Rzutuje sender na Player (tylko gdy playerOnly = true).
     */
    protected Player asPlayer(CommandSender sender) {
        return (Player) sender;
    }

    /**
     * Pobiera HexPlayer dla online gracza.
     */
    protected HexPlayer getHexPlayer(Player player) {
        return playerManager.get(player);
    }

    /**
     * Sprawdza minimalną liczbę argumentów.
     * Jeśli brakuje — wysyła INVALID_USAGE i zwraca false.
     */
    protected boolean requireArgs(CommandSender sender,
                                  String[] args,
                                  int min,
                                  String usage) {
        if (args.length < min) {
            messages.send(sender, MessageKey.INVALID_USAGE, "usage", usage);
            return false;
        }
        return true;
    }

    /**
     * Parsuje Double z args[index].
     * Jeśli błąd — wysyła INVALID_NUMBER i zwraca null.
     */
    protected Double parseDouble(CommandSender sender,
                                 String[] args,
                                 int index) {
        try {
            return Double.parseDouble(args[index]);
        } catch (NumberFormatException e) {
            messages.send(sender, MessageKey.INVALID_NUMBER);
            return null;
        }
    }
}