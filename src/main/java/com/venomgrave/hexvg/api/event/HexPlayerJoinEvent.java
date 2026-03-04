package com.venomgrave.hexvg.api.event;

import com.venomgrave.hexvg.api.player.HexPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Wywoływany gdy HexPlayer jest w pełni załadowany do cache.
 * Użyj zamiast PlayerJoinEvent gdy potrzebujesz danych HexVG.
 */
public class HexPlayerJoinEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final HexPlayer hexPlayer;
    private final boolean   firstJoin;

    public HexPlayerJoinEvent(HexPlayer hexPlayer, boolean firstJoin) {
        this.hexPlayer = hexPlayer;
        this.firstJoin = firstJoin;
    }

    public HexPlayer getHexPlayer() { return hexPlayer; }
    public boolean   isFirstJoin()  { return firstJoin; }

    @Override public HandlerList getHandlers()             { return HANDLERS; }
    public static HandlerList    getHandlerList()          { return HANDLERS; }
}