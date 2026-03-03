package com.venomgrave.hexvg;

import com.venomgrave.hexvg.api.HexVGCoreAPI;
import com.venomgrave.hexvg.api.audit.AuditLogger;
import com.venomgrave.hexvg.api.combat.CombatHook;
import com.venomgrave.hexvg.api.database.DatabaseService;
import com.venomgrave.hexvg.api.message.MessageProvider;
import com.venomgrave.hexvg.api.rank.RankHook;
import com.venomgrave.hexvg.api.session.SessionService;
import com.venomgrave.hexvg.config.CoreConfig;
import com.venomgrave.hexvg.impl.audit.DatabaseAuditLogger;
import com.venomgrave.hexvg.impl.database.DatabaseFactory;
import com.venomgrave.hexvg.impl.database.MigrationRunner;
import com.venomgrave.hexvg.impl.message.YamlMessageProvider;
import com.venomgrave.hexvg.impl.rank.LuckPermsRankHook;
import com.venomgrave.hexvg.impl.session.SessionListener;
import com.venomgrave.hexvg.impl.session.SessionServiceImpl;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class HexVGCore extends JavaPlugin {

    private static HexVGCore instance;

    // Serwisy
    private CoreConfig       coreConfig;
    private DatabaseService  databaseService;
    private AuditLogger      auditLogger;
    private MessageProvider  messageProvider;
    private SessionService   sessionService;
    private RankHook         rankHook;
    private CombatHook       combatHook;

    // Publiczne API
    private HexVGCoreAPI api;

    // ── Cykl życia pluginu ────────────────────────────────────────────────

    @Override
    public void onEnable() {
        instance = this;
        long start = System.currentTimeMillis();

        printHeader();

        // 1. Config
        saveDefaultConfig();
        if (!loadConfig()) {
            disable("Krytyczny błąd konfiguracji!");
            return;
        }

        // 2. Baza danych
        if (!initDatabase()) {
            disable("Nie można połączyć z bazą danych!");
            return;
        }

        // 3. Migracje
        if (!runMigrations()) {
            disable("Błąd migracji bazy danych!");
            return;
        }

        // 4. Serwisy
        initServices();

        // 5. Listenery
        registerListeners();

        // 6. Buduj API
        buildApi();

        long elapsed = System.currentTimeMillis() - start;
        getLogger().info("Uruchomiono pomyślnie (" + elapsed + "ms). "
                + "Baza: " + coreConfig.getDatabaseType().name());
    }

    @Override
    public void onDisable() {
        getLogger().info("[HexVG-Core] Zamykam...");

        // Flush sesji przed zamknięciem DB
        if (sessionService instanceof SessionServiceImpl impl) {
            impl.flushAll();
        }

        // Shutdown audit loggera (czeka na pending logi)
        if (auditLogger instanceof DatabaseAuditLogger dal) {
            dal.shutdown();
        }

        // Zamknij pulę połączeń
        if (databaseService != null) {
            databaseService.shutdown();
        }

        instance = null;
        getLogger().info("[HexVG-Core] Zamknięto.");
    }

    // ── Inicjalizacja ─────────────────────────────────────────────────────

    private boolean loadConfig() {
        coreConfig = new CoreConfig(this);
        return coreConfig.load(getConfig());
    }

    private boolean initDatabase() {
        try {
            databaseService = DatabaseFactory.create(coreConfig, getDataFolder(), getLogger());
            if (!databaseService.isConnected()) {
                getLogger().severe("Połączenie z bazą nie powiodło się.");
                return false;
            }
            return true;
        } catch (Exception e) {
            getLogger().severe("Błąd inicjalizacji DB: " + e.getMessage());
            if (coreConfig.isDebug()) e.printStackTrace();
            return false;
        }
    }

    private boolean runMigrations() {
        try {
            new MigrationRunner(databaseService, getLogger(), "core")
                    .run(List.of(
                            "V1__core_init.sql",
                            "V2__punishments_init.sql",
                            "V3__sessions.sql"
                    ));
            return true;
        } catch (Exception e) {
            getLogger().severe("Błąd migracji: " + e.getMessage());
            if (coreConfig.isDebug()) e.printStackTrace();
            return false;
        }
    }

    private void initServices() {
        messageProvider = new YamlMessageProvider(this, coreConfig.getDefaultLanguage());
        auditLogger     = new DatabaseAuditLogger(databaseService, getLogger(), coreConfig);
        sessionService  = new SessionServiceImpl(databaseService, getLogger(), coreConfig);
        rankHook        = initRankHook();
        combatHook      = null; // wstrzykiwany przez zewnętrzny plugin

        getLogger().info("[HexVG-Core] Serwisy gotowe.");
    }

    private RankHook initRankHook() {
        if (getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            getLogger().info("[HexVG-Core] LuckPerms znaleziony — ładuję RankHook.");
            return new LuckPermsRankHook(getLogger());
        }
        getLogger().warning("[HexVG-Core] LuckPerms nie znaleziony! "
                + "Hierarchia rang wyłączona.");
        return null;
    }

    private void registerListeners() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new SessionListener(sessionService, getLogger()), this);

        if (coreConfig.isDebug()) {
            getLogger().info("[HexVG-Core] Listenery zarejestrowane.");
        }
    }

    private void buildApi() {
        api = new HexVGCoreAPI(
                databaseService,
                auditLogger,
                messageProvider,
                sessionService,
                rankHook,
                combatHook
        );
    }

    // ── Pomocnicze ────────────────────────────────────────────────────────

    private void printHeader() {
        getLogger().info("==========================================");
        getLogger().info("  HexVG-Core v" + getDescription().getVersion());
        getLogger().info("  Autor: " + getDescription().getAuthors());
        getLogger().info("==========================================");
    }

    private void disable(String reason) {
        getLogger().severe(reason);
        getServer().getPluginManager().disablePlugin(this);
    }

    // ── API publiczne ─────────────────────────────────────────────────────

    public static HexVGCore getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "HexVG-Core nie jest załadowany!");
        }
        return instance;
    }

    public HexVGCoreAPI getAPI() {
        return api;
    }

    public void registerCombatHook(CombatHook hook) {
        this.combatHook = hook;
        if (api != null) api.setCombatHook(hook);
        getLogger().info("[HexVG-Core] CombatHook zarejestrowany: "
                + hook.getClass().getSimpleName());
    }

    // Skróty
    public DatabaseService  getDatabaseService()  { return databaseService; }
    public AuditLogger      getAuditLogger()       { return auditLogger; }
    public MessageProvider  getMessageProvider()   { return messageProvider; }
    public SessionService   getSessionService()    { return sessionService; }
    public RankHook         getRankHook()          { return rankHook; }
    public CombatHook       getCombatHook()        { return combatHook; }
    public CoreConfig       getCoreConfig()        { return coreConfig; }
}