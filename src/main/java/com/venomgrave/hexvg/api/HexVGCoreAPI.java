package com.venomgrave.hexvg.api;

import com.venomgrave.hexvg.api.audit.AuditLogger;
import com.venomgrave.hexvg.api.combat.CombatHook;
import com.venomgrave.hexvg.api.database.DatabaseService;
import com.venomgrave.hexvg.api.message.MessageProvider;
import com.venomgrave.hexvg.api.rank.RankHook;
import com.venomgrave.hexvg.api.session.SessionService;

public class HexVGCoreAPI {

    private final DatabaseService databaseService;
    private final AuditLogger     auditLogger;
    private final MessageProvider messageProvider;
    private final SessionService  sessionService;
    private final RankHook        rankHook;
    private       CombatHook      combatHook;

    public HexVGCoreAPI(
            DatabaseService databaseService,
            AuditLogger     auditLogger,
            MessageProvider messageProvider,
            SessionService  sessionService,
            RankHook        rankHook,
            CombatHook      combatHook
    ) {
        this.databaseService = databaseService;
        this.auditLogger     = auditLogger;
        this.messageProvider = messageProvider;
        this.sessionService  = sessionService;
        this.rankHook        = rankHook;
        this.combatHook      = combatHook;
    }

    public DatabaseService getDatabaseService()  { return databaseService; }
    public AuditLogger     getAuditLogger()       { return auditLogger; }
    public MessageProvider getMessageProvider()   { return messageProvider; }
    public SessionService  getSessionService()    { return sessionService; }
    public RankHook        getRankHook()          { return rankHook; }
    public CombatHook      getCombatHook()        { return combatHook; }

    /**
     * Tylko do użytku przez HexVGCore.registerCombatHook().
     */
    public void setCombatHook(CombatHook hook) {
        this.combatHook = hook;
    }
}