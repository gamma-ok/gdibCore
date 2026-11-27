package me.gdib.core.common.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class EventBus {

    private final Map<Class<?>, List<Consumer<?>>> listeners;
    private final JavaPlugin plugin;

    public EventBus(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.listeners = new HashMap<>();
    }

    // =========================================================
    // SUBSCRIPTIONS
    // =========================================================

    public <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
        ensureMainThread();

        Objects.requireNonNull(eventType, "Event type cannot be null");
        Objects.requireNonNull(listener, "Event listener cannot be null");

        List<Consumer<?>> eventListeners = listeners.computeIfAbsent(
                eventType,
                key -> new ArrayList<>()
        );

        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }

    public <T> void unsubscribe(Class<T> eventType, Consumer<T> listener) {
        ensureMainThread();

        Objects.requireNonNull(eventType, "Event type cannot be null");
        Objects.requireNonNull(listener, "Event listener cannot be null");

        List<Consumer<?>> eventListeners = listeners.get(eventType);

        if (eventListeners == null) {
            return;
        }

        eventListeners.remove(listener);

        if (eventListeners.isEmpty()) {
            listeners.remove(eventType);
        }
    }

    // =========================================================
    // EVENT PUBLISHING
    // =========================================================

    public <T> void publish(T event) {
        ensureMainThread();

        Objects.requireNonNull(event, "Event cannot be null");

        Class<?> eventType = event.getClass();

        List<Consumer<?>> eventListeners = listeners.get(eventType);

        if (eventListeners == null || eventListeners.isEmpty()) {
            return;
        }

        // Snapshot to prevent modifications during iteration.
        List<Consumer<?>> listenersCopy = new ArrayList<>(eventListeners);

        for (Consumer<?> listener : listenersCopy) {
            try {
                @SuppressWarnings("unchecked")
                Consumer<T> typedListener = (Consumer<T>) listener;

                typedListener.accept(event);

            } catch (RuntimeException exception) {
                plugin.getLogger().log(
                        Level.SEVERE,
                        "An error occurred while executing a listener for event "
                                + eventType.getSimpleName(),
                        exception
                );
            }
        }
    }

    // =========================================================
    // MANAGEMENT
    // =========================================================

    public void clear() {
        ensureMainThread();
        listeners.clear();
    }

    public int getListenerCount(Class<?> eventType) {
        ensureMainThread();

        Objects.requireNonNull(eventType, "Event type cannot be null");

        List<Consumer<?>> eventListeners = listeners.get(eventType);

        return eventListeners == null ? 0 : eventListeners.size();
    }

    // =========================================================
    // THREAD VALIDATION
    // =========================================================

    private void ensureMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException(
                    "EventBus operations must be executed on the main server thread."
            );
        }
    }
}
