package me.gdib.core.common.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

import java.util.function.Consumer;

import org.bukkit.plugin.java.JavaPlugin;

public final class EventBus {

    private final Map<Class<?>, List<Consumer<?>>> listeners;
    private final JavaPlugin plugin;

    public EventBus(JavaPlugin plugin) {

        this.plugin = Objects.requireNonNull(
            plugin,
            "Plugin cannot be null"
        );

        this.listeners = new HashMap<>();
    }

    public <T> void subscribe(
            Class<T> eventType,
            Consumer<T> listener) {

        Objects.requireNonNull(
            eventType,
            "Event type cannot be null"
        );

        Objects.requireNonNull(
            listener,
            "Listener cannot be null"
        );

        List<Consumer<?>> eventListeners =
            listeners.computeIfAbsent(
                eventType,
                key -> new ArrayList<>()
            );

        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }

    public <T> void unsubscribe(
            Class<T> eventType,
            Consumer<T> listener) {

        Objects.requireNonNull(
            eventType,
            "Event type cannot be null"
        );

        Objects.requireNonNull(
            listener,
            "Listener cannot be null"
        );

        List<Consumer<?>> eventListeners =
            listeners.get(eventType);

        if (eventListeners == null) {
            return;
        }

        eventListeners.remove(listener);

        if (eventListeners.isEmpty()) {
            listeners.remove(eventType);
        }
    }

    public <T> void publish(T event) {

        Objects.requireNonNull(
            event,
            "Event cannot be null"
        );

        Class<?> eventType = event.getClass();

        List<Consumer<?>> eventListeners =
            listeners.get(eventType);

        if (eventListeners == null || eventListeners.isEmpty()) {
            return;
        }

        // Create a snapshot to protect the current iteration.
        List<Consumer<?>> listenersCopy =
            new ArrayList<>(eventListeners);

        for (Consumer<?> listener : listenersCopy) {

            @SuppressWarnings("unchecked")
            Consumer<T> typedListener = (Consumer<T>) listener;

            try {

                typedListener.accept(event);

            } catch (RuntimeException exception) {

                plugin.getLogger().log(
                    Level.SEVERE,
                    "An event listener failed while handling "
                    + eventType.getName(),
                    exception
                );
            }
        }
    }

    public void clear() {
        listeners.clear();
    }

    public int getListenerCount(Class<?> eventType) {

        List<Consumer<?>> eventListeners =
            listeners.get(eventType);

        return eventListeners == null
            ? 0
            : eventListeners.size();
    }
}
