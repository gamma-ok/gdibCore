package me.gdib.core.modules.tab.animation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import me.gdib.core.kernel.ModuleContext;

public final class TabAnimationManager {

    private final ModuleContext context;

    private final Map<String, TabAnimation> animations;
    private final Map<String, Integer> currentIndices;
    private final Map<String, Integer> scheduledTasks;

    public TabAnimationManager(ModuleContext context) {
        this.context = Objects.requireNonNull(
                context,
                "Module context cannot be null"
        );

        this.animations = new LinkedHashMap<>();
        this.currentIndices = new LinkedHashMap<>();
        this.scheduledTasks = new LinkedHashMap<>();
    }

    public synchronized void registerAnimation(TabAnimation animation) {
        Objects.requireNonNull(animation, "Animation cannot be null");

        String name = animation.getName();

        if (animations.containsKey(name)) {
            throw new IllegalArgumentException(
                    "Animation '" + name + "' is already registered."
            );
        }

        animations.put(name, animation);
        currentIndices.put(name, 0);
    }

    public synchronized boolean unregisterAnimation(String name) {
        Objects.requireNonNull(name, "Animation name cannot be null");

        if (!animations.containsKey(name)) {
            return false;
        }

        stopAnimation(name);

        animations.remove(name);
        currentIndices.remove(name);

        return true;
    }

    public synchronized void startAnimation(String name) {
        Objects.requireNonNull(name, "Animation name cannot be null");

        TabAnimation animation = animations.get(name);

        if (animation == null) {
            throw new IllegalArgumentException(
                    "Animation '" + name + "' is not registered."
            );
        }

        if (scheduledTasks.containsKey(name)) {
            return;
        }

        long intervalTicks = Math.max(
                1L,
                (animation.getChangeInterval() + 49L) / 50L
        );

        int taskId = context.runTimer(
                () -> advanceAnimation(name),
                intervalTicks,
                intervalTicks
        );

        scheduledTasks.put(name, taskId);
    }

    public synchronized boolean stopAnimation(String name) {
        Objects.requireNonNull(name, "Animation name cannot be null");

        Integer taskId = scheduledTasks.remove(name);

        if (taskId == null) {
            return false;
        }

        context.cancelTask(taskId);
        currentIndices.put(name, 0);

        return true;
    }

    public synchronized void startAll() {
        for (String name : animations.keySet()) {
            startAnimation(name);
        }
    }

    public synchronized void stopAll() {
        for (String name : scheduledTasks.keySet().toArray(String[]::new)) {
            stopAnimation(name);
        }
    }

    public synchronized String getCurrentText(String name) {
        Objects.requireNonNull(name, "Animation name cannot be null");

        TabAnimation animation = animations.get(name);

        if (animation == null) {
            return null;
        }

        int index = currentIndices.getOrDefault(name, 0);

        return animation.getTexts().get(index);
    }

    public synchronized boolean isRunning(String name) {
        Objects.requireNonNull(name, "Animation name cannot be null");

        Integer taskId = scheduledTasks.get(name);

        if (taskId == null) {
            return false;
        }

        if (!context.getService(
                me.gdib.core.common.scheduler.SchedulerManager.class
        ).isActive(taskId)) {
            scheduledTasks.remove(name);
            currentIndices.put(name, 0);
            return false;
        }

        return true;
    }

    public synchronized boolean isRegistered(String name) {
        Objects.requireNonNull(name, "Animation name cannot be null");
        return animations.containsKey(name);
    }

    public synchronized int getAnimationCount() {
        return animations.size();
    }

    public synchronized int getRunningAnimationCount() {
        scheduledTasks.keySet().removeIf(name -> !isRunning(name));
        return scheduledTasks.size();
    }

    private synchronized void advanceAnimation(String name) {
        TabAnimation animation = animations.get(name);

        if (animation == null) {
            stopAnimation(name);
            return;
        }

        int nextIndex = currentIndices.getOrDefault(name, 0) + 1;

        if (nextIndex >= animation.getTextCount()) {
            nextIndex = 0;
        }

        currentIndices.put(name, nextIndex);
    }
}
