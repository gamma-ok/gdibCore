package me.gdib.core.modules.tab.layout.listener;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.gdib.core.kernel.ModuleContext;
import me.gdib.core.modules.tab.layout.TabLayoutUpdater;

public final class TabLayoutListener implements Listener {

    private final ModuleContext context;
    private final TabLayoutUpdater updater;

    private boolean registered;

    public TabLayoutListener(
            ModuleContext context,
            TabLayoutUpdater updater) {

        this.context = Objects.requireNonNull(
                context,
                "context cannot be null"
        );

        this.updater = Objects.requireNonNull(
                updater,
                "updater cannot be null"
        );
    }

    public void register() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException(
                    "TabLayoutListener must be registered on the main thread."
            );
        }

        if (registered) {
            return;
        }

        Bukkit.getPluginManager().registerEvents(
                this,
                context.getPlugin()
        );

        registered = true;
    }

    public void unregister() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException(
                    "TabLayoutListener must be unregistered on the main thread."
            );
        }

        if (!registered) {
            return;
        }

        HandlerList.unregisterAll(this);
        registered = false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        context.runLater(
                () -> updater.updatePlayer(event.getPlayer()),
                1L
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        context.runLater(
                () -> updater.updatePlayer(event.getPlayer()),
                1L
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        updater.clearPlayer(event.getPlayer());
    }

    public boolean isRegistered() {
        return registered;
    }
}
