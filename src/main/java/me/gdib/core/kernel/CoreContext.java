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
import me.gdib.core.common.config.CoreConfig;
import me.gdib.core.common.message.MessageManager;
import me.gdib.core.common.storage.StorageConfig;
import me.gdib.core.common.storage.StorageInitializer;
import me.gdib.core.common.storage.StorageManager;

public final class CoreContext {

	private final JavaPlugin plugin;
	private final ServiceRegistry serviceRegistry;
	private final ModuleManager moduleManager;
	private boolean shutdown;

	public CoreContext(JavaPlugin plugin) {
		this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");

		this.serviceRegistry = new ServiceRegistry();

		initializeServices();

		// Initialize module manager after shared services.
		this.moduleManager = new ModuleManager(this);
	}

	private void initializeServices() {

		// Event system
		EventBus eventBus = new EventBus(plugin);
		registerService(EventBus.class, eventBus);

		// Scheduler system
		SchedulerManager schedulerManager = new SchedulerManager(plugin);
		registerService(SchedulerManager.class, schedulerManager);

		// Animation system
		AnimationManager animationManager = new AnimationManager(schedulerManager);
		registerService(AnimationManager.class, animationManager);

		// Configuration system
		ConfigManager configManager = new ConfigManager(plugin);
		registerService(ConfigManager.class, configManager);

		CoreConfig coreConfig = new CoreConfig(configManager);
		registerService(CoreConfig.class, coreConfig);

		// Storage system
		StorageConfig storageConfig = new StorageConfig(configManager);
		registerService(StorageConfig.class, storageConfig);

		StorageManager storageManager =
				StorageInitializer.initialize(plugin, storageConfig);

		registerService(StorageManager.class, storageManager);

		// Message system
		MessageManager messageManager = new MessageManager(coreConfig.getPrefix());
		registerService(MessageManager.class, messageManager);

		// Cooldown system
		CooldownManager cooldownManager = new CooldownManager();
		registerService(CooldownManager.class, cooldownManager);

		// Placeholder system
		PlaceholderManager placeholderManager = new PlaceholderManager();
		registerService(PlaceholderManager.class, placeholderManager);

		// Player system
		PlayerManager playerManager = new PlayerManager();
		registerService(PlayerManager.class, playerManager);

		// Text formatting system
		TextFormatter textFormatter = new TextFormatter();
		registerService(TextFormatter.class, textFormatter);
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

		// Prevent multiple shutdown executions.
		if (shutdown) {
			return;
		}

		shutdown = true;

		plugin.getLogger().info("Starting gdibCore shutdown...");

		// 1. Disable all modules.
		executeShutdownStep("Disabling modules", () -> moduleManager.disableAll());

		executeShutdownStep("Initiating storage shutdown",
				this::shutdownStorage);

		// 2. Stop animations.
		executeShutdownStep("Stopping animations",
				() -> getOptionalService(AnimationManager.class).ifPresent(AnimationManager::stopAll));

		// 3. Cancel scheduled tasks.
		executeShutdownStep("Cancelling scheduled tasks",
				() -> getOptionalService(SchedulerManager.class).ifPresent(SchedulerManager::cancelAll));

		// 4. Clear event listeners.
		executeShutdownStep("Clearing event listeners",
				() -> getOptionalService(EventBus.class).ifPresent(EventBus::clear));

		// 5. Clear temporary data.
		executeShutdownStep("Clearing cooldowns",
				() -> getOptionalService(CooldownManager.class).ifPresent(CooldownManager::clear));

		executeShutdownStep("Clearing placeholders",
				() -> getOptionalService(PlaceholderManager.class).ifPresent(PlaceholderManager::clear));

		// 6. Save configurations.
		executeShutdownStep("Saving configurations",
				() -> getOptionalService(ConfigManager.class).ifPresent(ConfigManager::saveAll));

		// 7. Unload configurations.
		executeShutdownStep("Unloading configurations",
				() -> getOptionalService(ConfigManager.class).ifPresent(ConfigManager::unloadAll));

		// 8. Clear service registry.
		executeShutdownStep("Clearing service registry", () -> serviceRegistry.clear());

		plugin.getLogger().info("gdibCore shutdown process completed.");
	}

	private void shutdownStorage() {

		Optional<StorageManager> storage =
				getOptionalService(StorageManager.class);

		if (!storage.isPresent()) {
			return;
		}

		storage.get().shutdown().whenComplete((result, exception) -> {

			if (exception != null) {

				plugin.getLogger().severe(
						"Failed to close storage provider: "
								+ exception.getMessage()
				);

				exception.printStackTrace();

			} else {

				plugin.getLogger().info(
						"Storage provider closed successfully."
				);
			}
		});
	}

	private void executeShutdownStep(String operation, Runnable action) {

		try {

			action.run();

			plugin.getLogger().info(operation + " completed.");

		} catch (Exception exception) {

			plugin.getLogger().severe(operation + " failed: " + exception.getMessage());

			exception.printStackTrace();
		}
	}
}