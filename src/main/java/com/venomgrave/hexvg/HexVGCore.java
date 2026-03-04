package com.venomgrave.hexvg;

import com.venomgrave.hexvg.api.HexVGCoreAPI;
import com.venomgrave.hexvg.api.audit.AuditLogger;
import com.venomgrave.hexvg.api.combat.CombatHook;
import com.venomgrave.hexvg.api.data.PlayerDataService;
import com.venomgrave.hexvg.api.database.DatabaseService;
import com.venomgrave.hexvg.api.message.MessageProvider;
import com.venomgrave.hexvg.api.player.HexPlayerManager;
import com.venomgrave.hexvg.api.rank.RankHook;
import com.venomgrave.hexvg.api.session.SessionService;
import com.venomgrave.hexvg.config.CoreConfig;
import com.venomgrave.hexvg.impl.audit.DatabaseAuditLogger;
import com.venomgrave.hexvg.impl.data.PlayerDataServiceImpl;
import com.venomgrave.hexvg.impl.database.DatabaseFactory;
import com.venomgrave.hexvg.impl.database.MigrationRunner;
import com.venomgrave.hexvg.impl.message.YamlMessageProvider;
import com.venomgrave.hexvg.impl.placeholder.PapiHook;
import com.venomgrave.hexvg.impl.player.HexPlayerManagerImpl;
import com.venomgrave.hexvg.impl.rank.LuckPermsRankHook;
import com.venomgrave.hexvg.impl.session.SessionListener;
import com.venomgrave.hexvg.impl.session.SessionServiceImpl;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class HexVGCore extends JavaPlugin {

    // ── Singleton ─────────────────────────────────────────────────────────
    private static HexVGCore instance;

    // ── Konfiguracja ──────────────────────────────────────────────────────
    private CoreConfig coreConfig;

    // ── Serwisy ───────────────────────────────────────────────────────────
    private DatabaseService   databaseService;
    private AuditLogger       auditLogger;
    private MessageProvider   messageProvider;
    private SessionService    sessionService;
    private PlayerDataService playerDataService;
    private HexPlayerManager  playerManager;
    private RankHook          rankHook;
    private CombatHook        combatHook;

    // ── API ───────────────────────────────────────────────────────────────
    private HexVGCoreAPI api;

    // ══════════════════════════════════════════════════════════════════════
    // Cykl życia pluginu
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public void onEnable() {
        instance = this;
        long startTime = System.currentTimeMillis();

        printHeader();

        // ── Krok 1: Konfiguracja ──────────────────────────────────────────
        saveDefaultConfig();
        if (!loadConfig()) {
            disable("Krytyczny błąd konfiguracji — sprawdź config.yml!");
            return;
        }

        // ── Krok 2: Baza danych ───────────────────────────────────────────
        if (!initDatabase()) {
            disable("Nie można połączyć z bazą danych — sprawdź config.yml!");
            return;
        }

        // ── Krok 3: Migracje ──────────────────────────────────────────────
        if (!runMigrations()) {
            disable("Błąd migracji bazy danych — sprawdź logi!");
            return;
        }

        // ── Krok 4: Serwisy ───────────────────────────────────────────────
        initServices();

        // ── Krok 5: Listenery ─────────────────────────────────────────────
        registerListeners();

        // ── Krok 6: API ───────────────────────────────────────────────────
        buildApi();

        // ── Krok 7: Podsumowanie ──────────────────────────────────────────
        long elapsed = System.currentTimeMillis() - startTime;
        printFooter(elapsed);
    }

    @Override
    public void onDisable() {
        getLogger().info("[HexVG-Core] Zamykam...");

        // Kolejność ważna — najpierw flush danych, potem zamknięcie DB

        // 1. Flush wszystkich aktywnych sesji
        if (sessionService instanceof SessionServiceImpl impl) {
            impl.flushAll();
        }

        // 2. Shutdown audit loggera — czeka na pending wpisy (max 5s)
        if (auditLogger instanceof DatabaseAuditLogger dal) {
            dal.shutdown();
        }

        // 3. Zamknij pulę połączeń DB
        if (databaseService != null) {
            databaseService.shutdown();
        }

        instance = null;
        getLogger().info("[HexVG-Core] Zamknięto pomyślnie.");
    }

    // ══════════════════════════════════════════════════════════════════════
    // Inicjalizacja — kroki
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Wczytuje i waliduje config.yml.
     * Zwraca false jeśli znaleziono błąd krytyczny.
     */
    private boolean loadConfig() {
        try {
            coreConfig = new CoreConfig(this);
            boolean valid = coreConfig.load(getConfig());

            if (!valid) {
                getLogger().severe("config.yml zawiera błędy krytyczne.");
            }
            return valid;
        } catch (Exception e) {
            getLogger().severe("Wyjątek podczas ładowania configu: "
                    + e.getMessage());
            if (coreConfig != null && coreConfig.isDebug()) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Inicjalizuje połączenie z bazą danych.
     * Zwraca false jeśli połączenie się nie udało.
     */
    private boolean initDatabase() {
        try {
            databaseService = DatabaseFactory.create(
                    coreConfig,
                    getDataFolder(),
                    getLogger()
            );

            if (!databaseService.isConnected()) {
                getLogger().severe("Test połączenia z bazą nie powiódł się.");
                return false;
            }

            getLogger().info("[DB] Typ: " + coreConfig.getDatabaseType().name()
                    + " | Status: OK");
            return true;

        } catch (Exception e) {
            getLogger().severe("Błąd inicjalizacji bazy: " + e.getMessage());
            if (coreConfig.isDebug()) e.printStackTrace();
            return false;
        }
    }

    /**
     * Uruchamia migracje SQL z folderu resources/migrations/core/.
     * Zwraca false jeśli jakakolwiek migracja się nie powiodła.
     */
    private boolean runMigrations() {
        try {
            new MigrationRunner(databaseService, getLogger(), "core")
                    .run(List.of(
                            "V1__core_init.sql",
                            "V2__punishments_init.sql",
                            "V3__sessions.sql",
                            "V4__players.sql"
                    ));
            return true;
        } catch (Exception e) {
            getLogger().severe("Błąd migracji: " + e.getMessage());
            if (coreConfig.isDebug()) e.printStackTrace();
            return false;
        }
    }

    /**
     * Inicjalizuje wszystkie serwisy Core.
     * Kolejność ma znaczenie — serwisy mogą zależeć od siebie.
     */
    private void initServices() {
        // MessageProvider — potrzebny przez wszystkie inne serwisy do logów
        messageProvider = new YamlMessageProvider(
                this,
                coreConfig.getDefaultLanguage()
        );

        // AuditLogger — async zapis do DB
        auditLogger = new DatabaseAuditLogger(
                databaseService,
                getLogger(),
                coreConfig
        );

        // SessionService — zarządzanie sesjami i playtime
        sessionService = new SessionServiceImpl(
                databaseService,
                getLogger(),
                coreConfig
        );

        // PlayerDataService — profil gracza (nick, ban/mute cache)
        playerDataService = new PlayerDataServiceImpl(
                databaseService,
                getLogger(),
                coreConfig
        );

        // RankHook — opcjonalny, wymaga LuckPerms
        rankHook = initRankHook();

        // CombatHook — null do czasu wstrzyknięcia przez zewnętrzny plugin
        combatHook = null;

        // HexPlayerManager — cache online graczy
        // Inicjalizowany po serwisach których używa
        playerManager = new HexPlayerManagerImpl(
                sessionService,
                playerDataService,
                rankHook,
                getLogger()
        );

        getLogger().info("[HexVG-Core] Serwisy zainicjowane.");
    }

    /**
     * Próbuje załadować LuckPerms RankHook.
     * Jeśli LuckPerms nie jest dostępny — zwraca null i loguje ostrzeżenie.
     */
    private RankHook initRankHook() {
        if (getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            try {
                RankHook hook = new LuckPermsRankHook(getLogger());
                getLogger().info("[HexVG-Core] LuckPerms RankHook załadowany.");
                return hook;
            } catch (Exception e) {
                getLogger().warning("[HexVG-Core] Błąd ładowania LuckPerms: "
                        + e.getMessage());
                return null;
            }
        }

        getLogger().warning("[HexVG-Core] LuckPerms nie znaleziony! "
                + "Hierarchia rang i bypass będą niedostępne.");
        return null;
    }

    /**
     * Rejestruje wszystkie listenery Core.
     */
    private void registerListeners() {
        var pm = getServer().getPluginManager();

        // SessionListener — obsługuje join/quit, ładuje HexPlayer, fire events
        pm.registerEvents(
                new SessionListener(
                        this,
                        sessionService,
                        playerDataService,
                        playerManager,
                        getLogger()
                ),
                this
        );

        // PlaceholderAPI — rejestruj jeśli dostępne
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PapiHook(this).register();
            getLogger().info("[HexVG-Core] PlaceholderAPI hook zarejestrowany.");
        }

        if (coreConfig.isDebug()) {
            getLogger().info("[HexVG-Core] Listenery zarejestrowane.");
        }
    }

    /**
     * Buduje obiekt HexVGCoreAPI — publiczny punkt dostępu dla innych pluginów.
     */
    private void buildApi() {
        api = new HexVGCoreAPI(
                databaseService,
                auditLogger,
                messageProvider,
                sessionService,
                playerDataService,
                playerManager,
                rankHook,
                combatHook
        );

        if (coreConfig.isDebug()) {
            getLogger().info("[HexVG-Core] API zbudowane.");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Logi startowe
    // ══════════════════════════════════════════════════════════════════════

    private void printHeader() {
        String version = getDescription().getVersion();
        getLogger().info("+===========================================+");
        getLogger().info("  _    _           __     _______ ");
        getLogger().info(" | |  | |         \\ \\   / / ____|");
        getLogger().info(" | |__| | _____  __\\ \\_/ / |  __ ");
        getLogger().info(" |  __  |/ _ \\ \\/ / \\   /| | |_ |");
        getLogger().info(" | |  | |  __/>  <   | | | |__| |");
        getLogger().info(" |_|  |_|\\___/_/\\_\\  |_|  \\_____|");
        getLogger().info("                                   ");
        getLogger().info("  Core v" + version + " | by VenomGrave");
        getLogger().info("+===========================================+");
    }

    private void printFooter(long elapsedMs) {
        String dbType   = coreConfig.getDatabaseType().name();
        String language = coreConfig.getDefaultLanguage().toUpperCase();
        String papi     = getServer().getPluginManager()
                .getPlugin("PlaceholderAPI") != null ? "TAK" : "NIE";
        String luckPerms = rankHook != null ? "TAK" : "NIE";

        getLogger().info("+-------------------------------------------+");
        getLogger().info("  Status: URUCHOMIONY (" + elapsedMs + "ms)");
        getLogger().info("  Baza:   " + dbType);
        getLogger().info("  Język:  " + language);
        getLogger().info("  PAPI:   " + papi);
        getLogger().info("  LuckPerms: " + luckPerms);
        getLogger().info("+-------------------------------------------+");
    }

    // ══════════════════════════════════════════════════════════════════════
    // Publiczne API
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Pobiera instancję HexVGCore.
     *
     * @throws IllegalStateException jeśli Core nie jest załadowany
     */
    public static HexVGCore getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "HexVG-Core nie jest załadowany lub został już wyłączony!");
        }
        return instance;
    }

    /**
     * Pobiera centralne API HexVG.
     * Używaj tego w innych pluginach:
     *
     * HexVGCoreAPI api = HexVGCore.getInstance().getAPI();
     * api.getPlayerManager().get(player).getPlaytimeSeconds();
     */
    public HexVGCoreAPI getAPI() {
        return api;
    }

    /**
     * Rejestruje zewnętrzny CombatHook.
     * Wywoływane przez plugin PvP w jego onEnable().
     *
     * Przykład:
     * HexVGCore.getInstance().registerCombatHook(new MyCombatHook());
     */
    public void registerCombatHook(CombatHook hook) {
        if (hook == null) {
            getLogger().warning("[HexVG-Core] Próba rejestracji null CombatHook — ignoruję.");
            return;
        }
        this.combatHook = hook;
        if (api != null) api.setCombatHook(hook);
        getLogger().info("[HexVG-Core] CombatHook zarejestrowany: "
                + hook.getClass().getSimpleName());
    }

    // ── Skróty (convenience getters) ─────────────────────────────────────

    /** Skrót: HexVGCore.getInstance().getDatabaseService() */
    public DatabaseService getDatabaseService()     { return databaseService; }

    /** Skrót: HexVGCore.getInstance().getAuditLogger() */
    public AuditLogger getAuditLogger()              { return auditLogger; }

    /** Skrót: HexVGCore.getInstance().getMessageProvider() */
    public MessageProvider getMessageProvider()      { return messageProvider; }

    /** Skrót: HexVGCore.getInstance().getSessionService() */
    public SessionService getSessionService()        { return sessionService; }

    /** Skrót: HexVGCore.getInstance().getPlayerDataService() */
    public PlayerDataService getPlayerDataService()  { return playerDataService; }

    /** Skrót: HexVGCore.getInstance().getPlayerManager() */
    public HexPlayerManager getPlayerManager()       { return playerManager; }

    /** Skrót: HexVGCore.getInstance().getRankHook() */
    public RankHook getRankHook()                    { return rankHook; }

    /** Skrót: HexVGCore.getInstance().getCombatHook() */
    public CombatHook getCombatHook()                { return combatHook; }

    /** Dostęp do konfiguracji Core */
    public CoreConfig getCoreConfig()                { return coreConfig; }

    // ── Pomocnicze ────────────────────────────────────────────────────────

    /**
     * Wyłącza plugin z podanym komunikatem błędu.
     */
    private void disable(String reason) {
        getLogger().severe("╔══════════════════════════════════════════╗");
        getLogger().severe("║         KRYTYCZNY BŁĄD STARTU            ║");
        getLogger().severe("╠══════════════════════════════════════════╣");
        getLogger().severe("║ " + padRight(reason, 40) + " ║");
        getLogger().severe("╚══════════════════════════════════════════╝");
        getServer().getPluginManager().disablePlugin(this);
    }

    private String padRight(String s, int n) {
        if (s.length() >= n) return s.substring(0, n);
        return s + " ".repeat(n - s.length());
    }
}