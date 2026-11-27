package me.gdib.core.kernel;

import java.util.Objects;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.plugin.java.JavaPlugin;
import me.gdib.core.common.config.ConfigManager;
import me.gdib.core.common.event.EventBus;
import me.gdib.core.common.scheduler.SchedulerManager;

public final class ModuleContext {

	private final CoreContext coreContext;
	private final ModuleDescriptor descriptor;
	private final ModuleConfig moduleConfig;
	private final List<Runnable> eventUnsubscriptions;
	private final Set<Integer> scheduledTasks;

	public ModuleContext(CoreContext coreContext, ModuleDescriptor descriptor) {

		this.coreContext = Objects.requireNonNull(coreContext, "CoreContext cannot be null");

		this.descriptor = Objects.requireNonNull(descriptor, "Module descriptor cannot be null");

		ConfigManager configManager = coreContext.getService(ConfigManager.class);

		this.moduleConfig = new ModuleConfig(descriptor.getName(), configManager);

		this.eventUnsubscriptions = new ArrayList<>();

		this.scheduledTasks = new HashSet<>();
	}

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

	public <T> T getService(Class<T> type) {
		return coreContext.getService(type);
	}

	public <T> Optional<T> getOptionalService(Class<T> type) {
		return coreContext.getOptionalService(type);
	}

	public <T> void subscribe(Class<T> eventType, Consumer<T> listener) {

		EventBus eventBus = getService(EventBus.class);

		eventBus.subscribe(eventType, listener);

		eventUnsubscriptions.add(() -> eventBus.unsubscribe(eventType, listener));
	}

	public void unsubscribeAll() {

		for (Runnable unsubscription : eventUnsubscriptions) {
			unsubscription.run();
		}

		eventUnsubscriptions.clear();
	}

	public int getSubscriptionCount() {
		return eventUnsubscriptions.size();
	}

	public int runSync(Runnable task) {

		int taskId = getService(SchedulerManager.class).runSync(task);

		scheduledTasks.add(taskId);

		return taskId;
	}

	public int runLater(Runnable task, long delay) {

		int taskId = getService(SchedulerManager.class).runLater(task, delay);

		scheduledTasks.add(taskId);

		return taskId;
	}

	public int runTimer(Runnable task, long delay, long period) {

		int taskId = getService(SchedulerManager.class).runTimer(task, delay, period);

		scheduledTasks.add(taskId);

		return taskId;
	}

	public int runAsync(Runnable task) {

		int taskId = getService(SchedulerManager.class).runAsync(task);

		scheduledTasks.add(taskId);

		return taskId;
	}

	public int runAsyncLater(Runnable task, long delay) {

		int taskId = getService(SchedulerManager.class).runAsyncLater(task, delay);

		scheduledTasks.add(taskId);

		return taskId;
	}

	public int runAsyncTimer(Runnable task, long delay, long period) {

		int taskId = getService(SchedulerManager.class).runAsyncTimer(task, delay, period);

		scheduledTasks.add(taskId);

		return taskId;
	}

	public void cancelTask(int taskId) {

		if (scheduledTasks.remove(taskId)) {

			getService(SchedulerManager.class).cancelTask(taskId);
		}
	}

	public void cancelTasks() {

		SchedulerManager schedulerManager = getService(SchedulerManager.class);

		for (Integer taskId : scheduledTasks) {
			schedulerManager.cancelTask(taskId);
		}

		scheduledTasks.clear();
	}

	public int getScheduledTaskCount() {
		return scheduledTasks.size();
	}
}
