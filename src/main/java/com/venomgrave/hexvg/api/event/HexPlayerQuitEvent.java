package com.venomgrave.hexvg.api.event;

import com.venomgrave.hexvg.api.player.HexPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HexPlayerQuitEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final HexPlayer hexPlayer;
    private final long      sessionSeconds;

    public HexPlayerQuitEvent(HexPlayer hexPlayer, long sessionSeconds) {
        this.hexPlayer      = hexPlayer;
        this.sessionSeconds = sessionSeconds;
    }

    public HexPlayer getHexPlayer()      { return hexPlayer; }
    public long      getSessionSeconds() { return sessionSeconds; }

    @Override public HandlerList getHandlers()    { return HANDLERS; }
    public static HandlerList    getHandlerList() { return HANDLERS; }
}