package com.hexvg.core.events;

import com.hexvg.core.HexVGCore;
import com.hexvg.core.utils.Logger;
import org.bukkit.event.EventPriority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Centralny EventBus dla komunikacji między pluginami HexVG.
 *
 * Subskrybowanie:
 * <pre>
 *     EventBus bus = HexVGCore.getInstance().getEventBus();
 *     bus.subscribe(MójEvent.class, event -> { ... }, "NazwaPluginu");
 * </pre>
 *
 * Wywoływanie:
 * <pre>
 *     MójEvent event = new MójEvent(...);
 *     bus.call(event);
 *     if (!event.isCancelled()) { ... }
 * </pre>
 *
 * Wyrejestrowanie (w onDisable pluginu):
 * <pre>
 *     bus.unsubscribeAll("NazwaPluginu");
 * </pre>
 */
public class EventBus {

    private final HexVGCore core;
    private final Map<Class<? extends CoreEvent>, List<Subscription<?>>> subscribers = new HashMap<>();

    public EventBus(HexVGCore core) {
        this.core = core;
    }

    public <T extends CoreEvent> void subscribe(Class<T> eventClass, Consumer<T> handler, String owner) {
        subscribe(eventClass, handler, owner, EventPriority.NORMAL);
    }

    public <T extends CoreEvent> void subscribe(Class<T> eventClass,
                                                Consumer<T> handler,
                                                String owner,
                                                EventPriority priority) {
        subscribers
                .computeIfAbsent(eventClass, k -> new ArrayList<>())
                .add(new Subscription<>(handler, owner, priority));

        subscribers.get(eventClass).sort((a, b) ->
                Integer.compare(a.priority().getSlot(), b.priority().getSlot()));

        Logger.debug("[EventBus] " + owner + " subskrybuje " + eventClass.getSimpleName());
    }

    @SuppressWarnings("unchecked")
    public <T extends CoreEvent> T call(T event) {
        List<Subscription<?>> subs = subscribers.get(event.getClass());

        if (subs != null) {
            for (Subscription<?> subscription : subs) {
                try {
                    ((Subscription<T>) subscription).handler().accept(event);
                } catch (Exception e) {
                    Logger.error("[EventBus] Błąd w handlerze [" + subscription.owner()
                            + "] dla eventu " + event.getClass().getSimpleName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        core.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public <T extends CoreEvent> void callAsync(T event) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> call(event));
    }

    public void unsubscribeAll(String owner) {
        int count = 0;
        for (List<Subscription<?>> subs : subscribers.values()) {
            int before = subs.size();
            subs.removeIf(s -> s.owner().equals(owner));
            count += before - subs.size();
        }
        Logger.debug("[EventBus] Usunięto " + count + " subskrypcji dla: " + owner);
    }

    public int countSubscribers(Class<? extends CoreEvent> eventClass) {
        List<Subscription<?>> subs = subscribers.get(eventClass);
        return subs == null ? 0 : subs.size();
    }

    // Java 17 - record zamiast klasy
    private record Subscription<T extends CoreEvent>(
            Consumer<T> handler,
            String owner,
            EventPriority priority
    ) {}
}