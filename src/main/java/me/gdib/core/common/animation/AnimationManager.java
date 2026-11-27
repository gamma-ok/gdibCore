package me.gdib.core.common.animation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import me.gdib.core.common.scheduler.SchedulerManager;

public final class AnimationManager {

    private final SchedulerManager schedulerManager;
    private final Map<String, Integer> animations;

    public AnimationManager(SchedulerManager schedulerManager) {
        this.schedulerManager = Objects.requireNonNull(
                schedulerManager,
                "SchedulerManager cannot be null"
        );

        this.animations = new LinkedHashMap<>();
    }

    // =========================================================
    // ANIMATION MANAGEMENT
    // =========================================================

    public synchronized void start(
            String name,
            Runnable animation,
            long periodTicks
    ) {
        validateName(name);

        Objects.requireNonNull(animation, "Animation cannot be null");

        if (periodTicks <= 0) {
            throw new IllegalArgumentException(
                    "Animation period must be greater than zero"
            );
        }

        if (isRunning(name)) {
            throw new IllegalStateException(
                    "Animation is already running: " + name
            );
        }

        int taskId = schedulerManager.runTimer(
                animation,
                0L,
                periodTicks
        );

        animations.put(name, taskId);
    }

    public synchronized void stop(String name) {
        validateName(name);

        Integer taskId = animations.remove(name);

        if (taskId != null) {
            schedulerManager.cancelTask(taskId);
        }
    }

    public synchronized boolean isRunning(String name) {
        validateName(name);

        Integer taskId = animations.get(name);

        if (taskId == null) {
            return false;
        }

        boolean active = schedulerManager.isActive(taskId);

        if (!active) {
            animations.remove(name);
        }

        return active;
    }

    // =========================================================
    // INFORMATION
    // =========================================================

    public synchronized int getAnimationCount() {
        removeInactiveAnimations();

        return animations.size();
    }

    private void removeInactiveAnimations() {
        Iterator<Map.Entry<String, Integer>> iterator =
                animations.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();

            if (!schedulerManager.isActive(entry.getValue())) {
                iterator.remove();
            }
        }
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    public synchronized void stopAll() {
        List<Integer> taskIds = new ArrayList<>(animations.values());

        // Clear the registry before attempting cancellation.
        animations.clear();

        RuntimeException firstException = null;

        for (Integer taskId : taskIds) {
            try {
                schedulerManager.cancelTask(taskId);
            } catch (RuntimeException exception) {
                if (firstException == null) {
                    firstException = exception;
                } else {
                    firstException.addSuppressed(exception);
                }
            }
        }

        if (firstException != null) {
            throw firstException;
        }
    }

    public synchronized void clear() {
        stopAll();
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Animation name cannot be null or empty"
            );
        }
    }
}
