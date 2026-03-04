package com.venomgrave.hexvg.api;

import com.venomgrave.hexvg.api.audit.AuditLogger;
import com.venomgrave.hexvg.api.combat.CombatHook;
import com.venomgrave.hexvg.api.data.PlayerDataService;
import com.venomgrave.hexvg.api.database.DatabaseService;
import com.venomgrave.hexvg.api.message.MessageProvider;
import com.venomgrave.hexvg.api.player.HexPlayerManager;
import com.venomgrave.hexvg.api.rank.RankHook;
import com.venomgrave.hexvg.api.session.SessionService;

public class HexVGCoreAPI {

    private final DatabaseService   databaseService;
    private final AuditLogger       auditLogger;
    private final MessageProvider   messageProvider;
    private final SessionService    sessionService;
    private final PlayerDataService playerDataService;
    private final HexPlayerManager  playerManager;
    private final RankHook          rankHook;
    private       CombatHook        combatHook;

    public HexVGCoreAPI(
            DatabaseService   databaseService,
            AuditLogger       auditLogger,
            MessageProvider   messageProvider,
            SessionService    sessionService,
            PlayerDataService playerDataService,
            HexPlayerManager  playerManager,
            RankHook          rankHook,
            CombatHook        combatHook
    ) {
        this.databaseService   = databaseService;
        this.auditLogger       = auditLogger;
        this.messageProvider   = messageProvider;
        this.sessionService    = sessionService;
        this.playerDataService = playerDataService;
        this.playerManager     = playerManager;
        this.rankHook          = rankHook;
        this.combatHook        = combatHook;
    }

    public DatabaseService   getDatabaseService()   { return databaseService; }
    public AuditLogger       getAuditLogger()        { return auditLogger; }
    public MessageProvider   getMessageProvider()    { return messageProvider; }
    public SessionService    getSessionService()     { return sessionService; }
    public PlayerDataService getPlayerDataService()  { return playerDataService; }
    public HexPlayerManager  getPlayerManager()      { return playerManager; }
    public RankHook          getRankHook()           { return rankHook; }
    public CombatHook        getCombatHook()         { return combatHook; }

    public void setCombatHook(CombatHook hook) { this.combatHook = hook; }
}