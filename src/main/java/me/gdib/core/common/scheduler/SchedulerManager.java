package me.gdib.core.common.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public final class SchedulerManager {

	private final JavaPlugin plugin;
	private final BukkitScheduler scheduler;

	private final Map<Integer, BukkitTask> tasks;

	public SchedulerManager(JavaPlugin plugin) {
		this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");

		this.scheduler = Bukkit.getScheduler();
		this.tasks = new HashMap<>();
	}

	public int runSync(Runnable task) {
		return registerTask(scheduler.runTask(plugin, validateTask(task)));
	}

	public int runLater(Runnable task, long delay) {
		validateDelay(delay);

		return registerTask(scheduler.runTaskLater(plugin, validateTask(task), delay));
	}

	public int runTimer(Runnable task, long delay, long period) {

		validateDelay(delay);
		validatePeriod(period);

		return registerTask(scheduler.runTaskTimer(plugin, validateTask(task), delay, period));
	}

	public int runAsync(Runnable task) {
		return registerTask(scheduler.runTaskAsynchronously(plugin, validateTask(task)));
	}

	public int runAsyncLater(Runnable task, long delay) {
		validateDelay(delay);

		return registerTask(scheduler.runTaskLaterAsynchronously(plugin, validateTask(task), delay));
	}

	public int runAsyncTimer(Runnable task, long delay, long period) {

		validateDelay(delay);
		validatePeriod(period);

		return registerTask(scheduler.runTaskTimerAsynchronously(plugin, validateTask(task), delay, period));
	}

	private int registerTask(BukkitTask task) {
		int taskId = task.getTaskId();

		tasks.put(taskId, task);

		return taskId;
	}

	public void cancelTask(int taskId) {
		BukkitTask task = tasks.remove(taskId);

		if (task != null) {
			task.cancel();
		}
	}

	public void cancelAll() {
		List<BukkitTask> activeTasks = new ArrayList<>(tasks.values());

		for (BukkitTask task : activeTasks) {
			task.cancel();
		}

		tasks.clear();
	}

	public boolean isActive(int taskId) {
		BukkitTask task = tasks.get(taskId);

		if (task == null) {
			return false;
		}

		boolean active = scheduler.isQueued(taskId) || scheduler.isCurrentlyRunning(taskId);

		if (!active) {
			tasks.remove(taskId);
		}

		return active;
	}

	private Runnable validateTask(Runnable task) {
		return Objects.requireNonNull(task, "Task cannot be null");
	}

	private void validateDelay(long delay) {
		if (delay < 0) {
			throw new IllegalArgumentException("Delay cannot be negative");
		}
	}

	private void validatePeriod(long period) {
		if (period <= 0) {
			throw new IllegalArgumentException("Period must be greater than zero");
		}
	}
}
