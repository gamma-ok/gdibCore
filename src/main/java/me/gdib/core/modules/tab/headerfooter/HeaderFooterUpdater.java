package me.gdib.core.modules.tab.headerfooter;

import java.util.Objects;

import org.bukkit.entity.Player;

import me.gdib.core.common.player.PlayerManager;
import me.gdib.core.kernel.ModuleContext;

public final class HeaderFooterUpdater {

    private final ModuleContext context;
    private final HeaderFooterService headerFooterService;
    private final PlayerManager playerManager;

    private int taskId = -1;

    public HeaderFooterUpdater(
            ModuleContext context,
            HeaderFooterService headerFooterService,
            PlayerManager playerManager
    ) {
        this.context = Objects.requireNonNull(
                context,
                "ModuleContext cannot be null"
        );

        this.headerFooterService = Objects.requireNonNull(
                headerFooterService,
                "HeaderFooterService cannot be null"
        );

        this.playerManager = Objects.requireNonNull(
                playerManager,
                "PlayerManager cannot be null"
        );
    }

    public void start(long updateIntervalMillis) {
        if (updateIntervalMillis < 50) {
            throw new IllegalArgumentException(
                    "Update interval must be at least 50 milliseconds"
            );
        }

        if (isRunning()) {
            return;
        }

        long intervalTicks = Math.max(
                1L,
                (updateIntervalMillis + 49L) / 50L
        );

        taskId = context.runTimer(
                this::updateAll,
                intervalTicks,
                intervalTicks
        );
    }

    public void stop() {
        if (taskId == -1) {
            return;
        }

        context.cancelTask(taskId);
        taskId = -1;
    }

    public void updateAll() {
        for (Player player : playerManager.getOnlinePlayers()) {
            headerFooterService.update(player);
        }
    }

    public void updatePlayer(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");

        if (!player.isOnline()) {
            return;
        }

        headerFooterService.update(player);
    }

    public boolean isRunning() {
        if (taskId == -1) {
            return false;
        }

        boolean active = context.getService(
                me.gdib.core.common.scheduler.SchedulerManager.class
        ).isActive(taskId);

        if (!active) {
            taskId = -1;
        }

        return active;
    }
}
