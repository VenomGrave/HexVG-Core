package com.hexvg.core.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Bazowa klasa dla wszystkich eventów HexVG.
 * Pluginy zależne dziedziczą z tej klasy tworząc własne eventy.
 *
 * Przykład tworzenia własnego eventu:
 * <pre>
 *     public class PlayerEarnMoneyEvent extends CoreEvent {
 *         private static final HandlerList HANDLERS = new HandlerList();
 *
 *         private final UUID playerUuid;
 *         private double amount;
 *
 *         public PlayerEarnMoneyEvent(UUID playerUuid, double amount) {
 *             super(true); // true = cancellable
 *             this.playerUuid = playerUuid;
 *             this.amount = amount;
 *         }
 *
 *         // gettery, settery...
 *
 *         {@literal @}Override
 *         public HandlerList getHandlers() { return HANDLERS; }
 *         public static HandlerList getHandlerList() { return HANDLERS; }
 *     }
 * </pre>
 *
 * Przykład wywołania eventu przez EventBus:
 * <pre>
 *     EventBus bus = HexVGCore.getInstance().getEventBus();
 *
 *     PlayerEarnMoneyEvent event = new PlayerEarnMoneyEvent(player.getUniqueId(), 100.0);
 *     bus.call(event);
 *
 *     if (!event.isCancelled()) {
 *         // kontynuuj logikę
 *     }
 * </pre>
 */
public abstract class CoreEvent extends Event implements Cancellable {

    @Getter @Setter
    private boolean cancelled = false;

    private final boolean cancellable;

    protected CoreEvent() {
        this(false);
    }

    protected CoreEvent(boolean cancellable) {
        this.cancellable = cancellable;
    }

    @Override
    public void setCancelled(boolean cancel) {
        if (!cancellable && cancel) {
            throw new UnsupportedOperationException("Ten event nie jest cancellable!");
        }
        this.cancelled = cancel;
    }

    public boolean isCancellable() {
        return cancellable;
    }
}