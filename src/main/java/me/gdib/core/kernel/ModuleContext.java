package me.gdib.core.kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.plugin.java.JavaPlugin;
import me.gdib.core.common.command.CommandManager;
import me.gdib.core.common.command.CoreCommand;
import me.gdib.core.common.config.ConfigManager;
import me.gdib.core.common.event.EventBus;
import me.gdib.core.common.scheduler.SchedulerManager;

public final class ModuleContext {

	private final CoreContext coreContext;
	private final ModuleDescriptor descriptor;
	private final ModuleConfig moduleConfig;

	private final List<EventSubscription<?>> eventSubscriptions;
	private final List<Integer> scheduledTasks;
	private final List<String> registeredCommands;

	public ModuleContext(CoreContext coreContext, ModuleDescriptor descriptor) {
		this.coreContext = Objects.requireNonNull(coreContext, "Core context cannot be null");
		this.descriptor = Objects.requireNonNull(descriptor, "Module descriptor cannot be null");

		ConfigManager configManager = coreContext.getService(ConfigManager.class);
		this.moduleConfig = new ModuleConfig(descriptor.getName(), configManager);

		this.eventSubscriptions = new ArrayList<>();
		this.scheduledTasks = new ArrayList<>();
		this.registeredCommands = new ArrayList<>();
	}

	// =========================================================
	// CORE ACCESS
	// =========================================================

	public JavaPlugin getPlugin() {
		return coreContext.getPlugin();
	}

	public ModuleDescriptor getDescriptor() {
		return descriptor;
	}

	public ModuleManager getModuleManager() {
		return coreContext.getModuleManager();
	}

	public ModuleConfig getModuleConfig() {
		return moduleConfig;
	}

	public <T> T getService(Class<T> serviceClass) {
		return coreContext.getService(serviceClass);
	}

	public <T> java.util.Optional<T> getOptionalService(Class<T> serviceClass) {
		return coreContext.getOptionalService(serviceClass);
	}

	// =========================================================
	// EVENT SUBSCRIPTIONS
	// =========================================================

	public <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
		Objects.requireNonNull(eventType, "Event type cannot be null");
		Objects.requireNonNull(listener, "Event listener cannot be null");

		boolean alreadySubscribed = eventSubscriptions.stream()
				.anyMatch(subscription -> subscription.matches(eventType, listener));

		if (alreadySubscribed) {
			return;
		}

		EventBus eventBus = getService(EventBus.class);

		eventBus.subscribe(eventType, listener);
		eventSubscriptions.add(new EventSubscription<>(eventType, listener));
	}

	public void unsubscribeAll() {
		EventBus eventBus = getService(EventBus.class);

		List<EventSubscription<?>> subscriptionsCopy = new ArrayList<>(eventSubscriptions);

		eventSubscriptions.clear();

		for (EventSubscription<?> subscription : subscriptionsCopy) {
			try {
				subscription.unsubscribe(eventBus);
			} catch (RuntimeException exception) {
				getPlugin().getLogger().severe(
						"Could not unregister an event subscription from module '" + descriptor.getName() + "'.");
				exception.printStackTrace();
			}
		}
	}

	public int getSubscriptionCount() {
		return eventSubscriptions.size();
	}

	private static final class EventSubscription<T> {

		private final Class<T> eventType;
		private final Consumer<T> listener;

		private EventSubscription(Class<T> eventType, Consumer<T> listener) {
			this.eventType = eventType;
			this.listener = listener;
		}

		private boolean matches(Class<?> eventType, Consumer<?> listener) {
			return this.eventType.equals(eventType) && this.listener.equals(listener);
		}

		private void unsubscribe(EventBus eventBus) {
			eventBus.unsubscribe(eventType, listener);
		}
	}

	// =========================================================
	// SCHEDULED TASKS
	// =========================================================

	public int runSync(Runnable task) {
		Objects.requireNonNull(task, "Task cannot be null");

		int taskId = getService(SchedulerManager.class).runSync(task);
		scheduledTasks.add(taskId);

		return taskId;
	}

	public int runLater(Runnable task, long delay) {
		Objects.requireNonNull(task, "Task cannot be null");

		int taskId = getService(SchedulerManager.class).runLater(task, delay);
		scheduledTasks.add(taskId);

		return taskId;
	}

	public int runTimer(Runnable task, long delay, long period) {
		Objects.requireNonNull(task, "Task cannot be null");

		int taskId = getService(SchedulerManager.class).runTimer(task, delay, period);
		scheduledTasks.add(taskId);

		return taskId;
	}

	public int runAsync(Runnable task) {
		Objects.requireNonNull(task, "Task cannot be null");

		int taskId = getService(SchedulerManager.class).runAsync(task);
		scheduledTasks.add(taskId);

		return taskId;
	}

	public int runAsyncLater(Runnable task, long delay) {
		Objects.requireNonNull(task, "Task cannot be null");

		int taskId = getService(SchedulerManager.class).runAsyncLater(task, delay);
		scheduledTasks.add(taskId);

		return taskId;
	}

	public int runAsyncTimer(Runnable task, long delay, long period) {
		Objects.requireNonNull(task, "Task cannot be null");

		int taskId = getService(SchedulerManager.class).runAsyncTimer(task, delay, period);

		scheduledTasks.add(taskId);

		return taskId;
	}

	public void cancelTask(int taskId) {
		getService(SchedulerManager.class).cancelTask(taskId);
		scheduledTasks.remove(Integer.valueOf(taskId));
	}

	public void cancelTasks() {
		SchedulerManager schedulerManager = getService(SchedulerManager.class);

		for (int taskId : new ArrayList<>(scheduledTasks)) {
			try {
				schedulerManager.cancelTask(taskId);
			} catch (RuntimeException exception) {
				getPlugin().getLogger().severe(
						"Could not cancel scheduled task " + taskId + " from module '" + descriptor.getName() + "'.");
				exception.printStackTrace();
			}
		}

		scheduledTasks.clear();
	}

	public int getScheduledTaskCount() {

		SchedulerManager schedulerManager = getService(SchedulerManager.class);

		scheduledTasks.removeIf(taskId -> !schedulerManager.isActive(taskId));

		return scheduledTasks.size();
	}

	// =========================================================
	// COMMAND REGISTRATION
	// =========================================================

	public void registerCommand(CoreCommand command) {
		Objects.requireNonNull(command, "Command cannot be null");

		CommandManager commandManager = getService(CommandManager.class);

		commandManager.registerCommand(command);

		String commandName = command.getName().toLowerCase(Locale.ROOT);

		if (!registeredCommands.contains(commandName)) {
			registeredCommands.add(commandName);
		}
	}

	public void unregisterCommand(String commandName) {
		Objects.requireNonNull(commandName, "Command name cannot be null");

		String normalizedName = commandName.toLowerCase(Locale.ROOT);

		if (!registeredCommands.remove(normalizedName)) {
			return;
		}

		getService(CommandManager.class).unregisterCommand(normalizedName);
	}

	public void unregisterCommands() {
		CommandManager commandManager = getService(CommandManager.class);

		for (String commandName : new ArrayList<>(registeredCommands)) {
			try {
				commandManager.unregisterCommand(commandName);
			} catch (RuntimeException exception) {
				getPlugin().getLogger().severe("Could not unregister command '" + commandName + "' from module '"
						+ descriptor.getName() + "'.");
				exception.printStackTrace();
			}
		}

		registeredCommands.clear();
	}

	public int getRegisteredCommandCount() {
		return registeredCommands.size();
	}
}
