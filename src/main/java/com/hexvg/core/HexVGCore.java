package com.hexvg.core;

import com.hexvg.core.commands.CoreCommand;
import com.hexvg.core.config.ConfigManager;
import com.hexvg.core.cooldown.CooldownManager;
import com.hexvg.core.database.DatabaseManager;
import com.hexvg.core.messages.MessageManager;
import com.hexvg.core.player.PlayerDataManager;
import com.hexvg.core.utils.Logger;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class HexVGCore extends JavaPlugin {

    @Getter
    private static HexVGCore instance;

    @Getter
    private ConfigManager configManager;

    @Getter
    private DatabaseManager databaseManager;

    @Getter
    private MessageManager messageManager;

    @Getter
    private CooldownManager cooldownManager;

    @Getter
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        instance = this;

        Logger.info("&6==============================");
        Logger.info("&6   HexVG-Core v" + getDescription().getVersion());
        Logger.info("&6==============================");

        // Inicjalizacja menedżerów w odpowiedniej kolejności
        loadManagers();

        // Rejestracja komend
        registerCommands();

        Logger.info("&aHexVG-Core uruchomiony pomyślnie!");
    }

    @Override
    public void onDisable() {
        // Zamknięcie połączenia z DB
        if (databaseManager != null) {
            databaseManager.close();
        }

        Logger.info("&cHexVG-Core wyłączony.");
    }

    /**
     * Inicjalizuje wszystkie menedżery w odpowiedniej kolejności.
     */
    private void loadManagers() {
        // 1. Konfiguracja (pierwsze, bo reszta z niej korzysta)
        configManager = new ConfigManager(this);
        configManager.load();

        // 2. Logger (ustawia tryb debug z konfiguracji)
        Logger.setDebug(configManager.isDebug());

        // 3. Wiadomości
        messageManager = new MessageManager(this);
        messageManager.load();

        // 4. Baza danych
        databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        // 5. Cooldown
        cooldownManager = new CooldownManager();

        // 6. Dane graczy
        playerDataManager = new PlayerDataManager(this);
    }

    /**
     * Rejestruje komendy pluginu.
     */
    private void registerCommands() {
        getCommand("hexcore").setExecutor(new CoreCommand(this));
    }

    /**
     * Przeładowuje wszystkie menedżery.
     * Wywoływane przez inne pluginy lub komendę /hexcore reload
     */
    public void reload() {
        configManager.load();
        messageManager.load();
        Logger.setDebug(configManager.isDebug());
        Logger.info("&aKonfiguracja przeładowana.");
    }

    /**
     * Zwraca instancję Core dla innych pluginów.
     * Użycie: HexVGCore core = HexVGCore.getInstance();
     */
    public static HexVGCore getCore() {
        return instance;
    }
}