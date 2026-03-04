package com.venomgrave.hexvg.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Wywoływany przed nałożeniem kary (ban, mute, kick, warn).
 * Można anulować — np. plugin antygrief może blokować ban.
 */
public class HexPunishEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    public enum PunishType { BAN, TEMP_BAN, MUTE, KICK, WARN }

    private final UUID      targetUuid;
    private final String    targetName;
    private final UUID      staffUuid;
    private final String    staffName;
    private final PunishType type;
    private       String    reason;
    private       boolean   cancelled;

    public HexPunishEvent(UUID targetUuid, String targetName,
                          UUID staffUuid,  String staffName,
                          PunishType type, String reason) {
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.staffUuid  = staffUuid;
        this.staffName  = staffName;
        this.type       = type;
        this.reason     = reason;
        this.cancelled  = false;
    }

    public UUID       getTargetUuid() { return targetUuid; }
    public String     getTargetName() { return targetName; }
    public UUID       getStaffUuid()  { return staffUuid; }
    public String     getStaffName()  { return staffName; }
    public PunishType getType()       { return type; }
    public String     getReason()     { return reason; }
    public void       setReason(String reason) { this.reason = reason; }

    @Override public boolean isCancelled()          { return cancelled; }
    @Override public void    setCancelled(boolean c) { this.cancelled = c; }
    @Override public HandlerList getHandlers()       { return HANDLERS; }
    public static HandlerList    getHandlerList()    { return HANDLERS; }
}