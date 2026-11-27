package me.gdib.core.kernel;

import java.util.Objects;
import java.util.Optional;

import org.bukkit.plugin.java.JavaPlugin;

import me.gdib.core.common.animation.AnimationManager;
import me.gdib.core.common.config.ConfigManager;
import me.gdib.core.common.cooldown.CooldownManager;
import me.gdib.core.common.event.EventBus;
import me.gdib.core.common.placeholder.PlaceholderManager;
import me.gdib.core.common.player.PlayerManager;
import me.gdib.core.common.scheduler.SchedulerManager;
import me.gdib.core.common.text.TextFormatter;

public final class CoreContext {

    private final JavaPlugin plugin;
    private final ServiceRegistry serviceRegistry;
    private final ModuleManager moduleManager;

    public CoreContext(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(
            plugin,
            "Plugin cannot be null"
        );

        this.serviceRegistry = new ServiceRegistry();

        initializeServices();

        // Initialize module manager after shared services.
        this.moduleManager = new ModuleManager(this);
    }

    private void initializeServices() {

        EventBus eventBus = new EventBus();
        registerService(EventBus.class, eventBus);

        SchedulerManager schedulerManager =
            new SchedulerManager(plugin);

        registerService(
            SchedulerManager.class,
            schedulerManager
        );

        AnimationManager animationManager =
            new AnimationManager(schedulerManager);

        registerService(
            AnimationManager.class,
            animationManager
        );

        ConfigManager configManager =
            new ConfigManager(plugin);

        registerService(
            ConfigManager.class,
            configManager
        );

        CooldownManager cooldownManager =
            new CooldownManager();

        registerService(
            CooldownManager.class,
            cooldownManager
        );

        PlaceholderManager placeholderManager =
            new PlaceholderManager();

        registerService(
            PlaceholderManager.class,
            placeholderManager
        );

        PlayerManager playerManager =
            new PlayerManager();

        registerService(
            PlayerManager.class,
            playerManager
        );

        TextFormatter textFormatter =
            new TextFormatter();

        registerService(
            TextFormatter.class,
            textFormatter
        );
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
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

        // Disable modules before shutting down shared services.
        moduleManager.disableAll();

        // Stop animations.
        getOptionalService(AnimationManager.class)
            .ifPresent(AnimationManager::stopAll);

        // Cancel scheduled tasks.
        getOptionalService(SchedulerManager.class)
            .ifPresent(SchedulerManager::cancelAll);

        // Clear internal events.
        getOptionalService(EventBus.class)
            .ifPresent(EventBus::clear);

        // Clear cooldowns.
        getOptionalService(CooldownManager.class)
            .ifPresent(CooldownManager::clear);

        // Clear placeholders.
        getOptionalService(PlaceholderManager.class)
            .ifPresent(PlaceholderManager::clear);

        // Unload configurations.
        getOptionalService(ConfigManager.class)
            .ifPresent(ConfigManager::unloadAll);

        // Clear registered services.
        serviceRegistry.clear();
    }
}
