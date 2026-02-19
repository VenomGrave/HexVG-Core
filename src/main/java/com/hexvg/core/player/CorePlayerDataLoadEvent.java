package com.hexvg.core.player;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event wywoływany gdy dane gracza zostaną załadowane z bazy danych do cache.
 * Inne pluginy mogą nasłuchiwać tego eventu, żeby wiedzieć kiedy dane są gotowe.
 *
 * Przykład użycia:
 * <pre>
 *     {@literal @}EventHandler
 *     public void onPlayerDataLoad(CorePlayerDataLoadEvent event) {
 *         CorePlayerData data = event.getPlayerData();
 *         // inicjalizuj własne dane dla gracza
 *     }
 * </pre>
 */
@Getter
public class CorePlayerDataLoadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final CorePlayerData playerData;

    public CorePlayerDataLoadEvent(CorePlayerData playerData) {
        this.playerData = playerData;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}