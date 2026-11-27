package me.gdib.core.kernel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ServiceRegistry {

    private final Map<Class<?>, Object> services;

    public ServiceRegistry() {
        this.services = new HashMap<>();
    }

    // =========================================================
    // SERVICE REGISTRATION
    // =========================================================

    public synchronized <T> void register(Class<T> type, T service) {

        Objects.requireNonNull(type, "Service type cannot be null");
        Objects.requireNonNull(service, "Service cannot be null");

        if (!type.isInstance(service)) {
            throw new IllegalArgumentException(
                    "Service does not match the registered type: "
                            + type.getName()
            );
        }

        if (services.containsKey(type)) {
            throw new IllegalStateException(
                    "Service is already registered: " + type.getName()
            );
        }

        services.put(type, service);
    }

    // =========================================================
    // SERVICE RETRIEVAL
    // =========================================================

    public synchronized <T> T get(Class<T> type) {

        Objects.requireNonNull(type, "Service type cannot be null");

        Object service = services.get(type);

        if (service == null) {
            throw new IllegalStateException(
                    "Service is not registered: " + type.getName()
            );
        }

        return type.cast(service);
    }

    public synchronized <T> Optional<T> getOptional(Class<T> type) {

        Objects.requireNonNull(type, "Service type cannot be null");

        Object service = services.get(type);

        return Optional.ofNullable(service)
                .map(type::cast);
    }

    // =========================================================
    // SERVICE INFORMATION
    // =========================================================

    public synchronized boolean isRegistered(Class<?> type) {

        Objects.requireNonNull(type, "Service type cannot be null");

        return services.containsKey(type);
    }

    // =========================================================
    // SERVICE REMOVAL
    // =========================================================

    public synchronized <T> void unregister(Class<T> type) {

        Objects.requireNonNull(type, "Service type cannot be null");

        services.remove(type);
    }

    public synchronized void clear() {
        services.clear();
    }
}
