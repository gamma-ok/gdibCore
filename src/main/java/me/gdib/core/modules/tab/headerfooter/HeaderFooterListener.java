package me.gdib.core.modules.tab.headerfooter;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import me.gdib.core.kernel.ModuleContext;

public final class HeaderFooterListener implements Listener {

    private final ModuleContext context;
    private final HeaderFooterUpdater updater;

    private boolean registered;

    public HeaderFooterListener(
            ModuleContext context,
            HeaderFooterUpdater updater
    ) {
        this.context = Objects.requireNonNull(
                context,
                "ModuleContext cannot be null"
        );

        this.updater = Objects.requireNonNull(
                updater,
                "HeaderFooterUpdater cannot be null"
        );
    }

    public void register() {
        if (registered) {
            return;
        }

        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException(
                    "Listener must be registered from the main thread"
            );
        }

        Bukkit.getPluginManager().registerEvents(
                this,
                context.getPlugin()
        );

        registered = true;
    }

    public void unregister() {
        if (!registered) {
            return;
        }

        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException(
                    "Listener must be unregistered from the main thread"
            );
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

    public boolean isRegistered() {
        return registered;
    }
}
