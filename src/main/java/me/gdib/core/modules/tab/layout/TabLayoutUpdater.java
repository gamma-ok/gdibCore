package me.gdib.core.modules.tab.layout;

import java.util.List;
import java.util.Objects;
import java.util.Collection;
import org.bukkit.entity.Player;

import me.gdib.core.common.player.PlayerManager;
import me.gdib.core.common.scheduler.SchedulerManager;
import me.gdib.core.kernel.ModuleContext;
import me.gdib.core.modules.tab.layout.model.TabLayoutEntry;
import me.gdib.core.modules.tab.packet.TabLayoutPacketAdapter;

public final class TabLayoutUpdater {

    private final ModuleContext context;
    private final TabLayoutBuilder layoutBuilder;
    private final TabLayoutPacketAdapter packetAdapter;
    private final PlayerManager playerManager;

    private int taskId = -1;

    public TabLayoutUpdater(
            ModuleContext context,
            TabLayoutBuilder layoutBuilder,
            TabLayoutPacketAdapter packetAdapter,
            PlayerManager playerManager) {

        this.context = Objects.requireNonNull(
                context,
                "context cannot be null"
        );

        this.layoutBuilder = Objects.requireNonNull(
                layoutBuilder,
                "layoutBuilder cannot be null"
        );

        this.packetAdapter = Objects.requireNonNull(
                packetAdapter,
                "packetAdapter cannot be null"
        );

        this.playerManager = Objects.requireNonNull(
                playerManager,
                "playerManager cannot be null"
        );
    }

    public void start(long updateIntervalMillis) {
        if (updateIntervalMillis < 50) {
            throw new IllegalArgumentException(
                    "Update interval must be at least 50 milliseconds."
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
    	Collection<Player> onlinePlayers = playerManager.getOnlinePlayers();

        if (onlinePlayers.isEmpty()) {
            return;
        }

        List<TabLayoutEntry> entries = layoutBuilder.build(onlinePlayers);

        for (Player player : onlinePlayers) {
            if (player == null || !player.isOnline()) {
                continue;
            }

            packetAdapter.updateLayout(player, entries);
        }
    }

    public void updatePlayer(Player player) {
        Objects.requireNonNull(player, "player cannot be null");

        if (!player.isOnline()) {
            return;
        }

        Collection<Player> onlinePlayers = playerManager.getOnlinePlayers();
        List<TabLayoutEntry> entries = layoutBuilder.build(onlinePlayers);

        packetAdapter.updateLayout(player, entries);
    }

    public void clearPlayer(Player player) {
        Objects.requireNonNull(player, "player cannot be null");

        packetAdapter.clearLayout(player);
    }

    public boolean isRunning() {
        if (taskId == -1) {
            return false;
        }

        SchedulerManager schedulerManager = context.getService(
                SchedulerManager.class
        );

        return schedulerManager.isActive(taskId);
    }
}
