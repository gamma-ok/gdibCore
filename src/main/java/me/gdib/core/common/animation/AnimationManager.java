package me.gdib.core.common.animation;

import java.util.LinkedHashMap;
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

    public void start(
            String name,
            Runnable animation,
            long periodTicks) {

        validateName(name);

        Objects.requireNonNull(
            animation,
            "Animation cannot be null"
        );

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

    public void stop(String name) {
        validateName(name);

        Integer taskId = animations.remove(name);

        if (taskId != null) {
            schedulerManager.cancelTask(taskId);
        }
    }

    public boolean isRunning(String name) {
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

    public int getAnimationCount() {
        return animations.size();
    }

    public void stopAll() {
        for (Integer taskId : animations.values()) {
            schedulerManager.cancelTask(taskId);
        }

        animations.clear();
    }

    public void clear() {
        stopAll();
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Animation name cannot be null or empty"
            );
        }
    }
}
