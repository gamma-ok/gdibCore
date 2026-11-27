package me.gdib.core.common.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
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
        this.tasks = new ConcurrentHashMap<>();
    }

    // =========================================================
    // SYNCHRONOUS TASKS
    // =========================================================

    public synchronized int runSync(Runnable task) {
        return scheduleOneShot(
                wrappedTask -> scheduler.runTask(plugin, wrappedTask),
                task
        );
    }

    public synchronized int runLater(Runnable task, long delay) {
        validateDelay(delay);

        return scheduleOneShot(
                wrappedTask -> scheduler.runTaskLater(plugin, wrappedTask, delay),
                task
        );
    }

    public synchronized int runTimer(Runnable task, long delay, long period) {
        validateDelay(delay);
        validatePeriod(period);

        return registerTask(
                scheduler.runTaskTimer(plugin, validateTask(task), delay, period)
        );
    }

    // =========================================================
    // ASYNCHRONOUS TASKS
    // =========================================================

    public synchronized int runAsync(Runnable task) {
        return scheduleOneShot(
                wrappedTask -> scheduler.runTaskAsynchronously(plugin, wrappedTask),
                task
        );
    }

    public synchronized int runAsyncLater(Runnable task, long delay) {
        validateDelay(delay);

        return scheduleOneShot(
                wrappedTask -> scheduler.runTaskLaterAsynchronously(
                        plugin, wrappedTask, delay
                ),
                task
        );
    }

    public synchronized int runAsyncTimer(Runnable task, long delay, long period) {
        validateDelay(delay);
        validatePeriod(period);

        return registerTask(
                scheduler.runTaskTimerAsynchronously(
                        plugin, validateTask(task), delay, period
                )
        );
    }

    // =========================================================
    // TASK REGISTRATION
    // =========================================================

    private int scheduleOneShot(
            Function<Runnable, BukkitTask> schedulerFunction,
            Runnable task
    ) {
        Objects.requireNonNull(schedulerFunction, "Scheduler function cannot be null");

        AtomicReference<BukkitTask> taskReference = new AtomicReference<>();
        AtomicBoolean completed = new AtomicBoolean(false);

        Runnable wrappedTask = () -> {
            try {
                validateTask(task).run();
            } finally {
                completed.set(true);

                BukkitTask scheduledTask = taskReference.get();

                if (scheduledTask != null) {
                    tasks.remove(scheduledTask.getTaskId(), scheduledTask);
                }
            }
        };

        BukkitTask scheduledTask = schedulerFunction.apply(wrappedTask);

        int taskId = scheduledTask.getTaskId();

        tasks.put(taskId, scheduledTask);
        taskReference.set(scheduledTask);

        // Handles tasks that finish before their registration completes.
        if (completed.get()) {
            tasks.remove(taskId, scheduledTask);
        }

        return taskId;
    }

    private int registerTask(BukkitTask task) {
        int taskId = task.getTaskId();

        tasks.put(taskId, task);

        return taskId;
    }

    // =========================================================
    // TASK CANCELLATION
    // =========================================================

    public synchronized void cancelTask(int taskId) {
        BukkitTask task = tasks.remove(taskId);

        if (task != null) {
            task.cancel();
        }
    }

    public synchronized void cancelAll() {
        List<BukkitTask> activeTasks = new ArrayList<>(tasks.values());

        for (BukkitTask task : activeTasks) {
            try {
                task.cancel();
            } catch (RuntimeException exception) {
                plugin.getLogger().severe(
                        "Could not cancel scheduled task " + task.getTaskId()
                );

                exception.printStackTrace();
            }
        }

        tasks.clear();
    }

    // =========================================================
    // TASK INFORMATION
    // =========================================================

    public synchronized boolean isActive(int taskId) {
        BukkitTask task = tasks.get(taskId);

        if (task == null) {
            return false;
        }

        boolean active = scheduler.isQueued(taskId)
                || scheduler.isCurrentlyRunning(taskId);

        if (!active) {
            tasks.remove(taskId, task);
        }

        return active;
    }

    public synchronized int getScheduledTaskCount() {
        return tasks.size();
    }

    // =========================================================
    // VALIDATION
    // =========================================================

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
            throw new IllegalArgumentException(
                    "Period must be greater than zero"
            );
        }
    }
}
