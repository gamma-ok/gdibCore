package me.gdib.core.kernel;

import java.util.Objects;
import java.util.Optional;

import org.bukkit.plugin.java.JavaPlugin;

public final class CoreContext {

    private final JavaPlugin plugin;
    private final ServiceRegistry serviceRegistry;

    public CoreContext(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(
            plugin,
            "Plugin cannot be null"
        );

        this.serviceRegistry = new ServiceRegistry();
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public <T> void registerService(Class<T> type, T service) {
        serviceRegistry.register(type, service);
    }

    public <T> T getService(Class<T> type) {
        return serviceRegistry.get(type);
    }

    public <T> Optional<T> getOptionalService(Class<T> type) {
        return serviceRegistry.getOptional(type);
    }

    public void shutdown() {
        serviceRegistry.clear();
    }
}
