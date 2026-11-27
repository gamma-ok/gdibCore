package me.gdib.core.common.message;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.gdib.core.common.config.CoreConfig;

public final class MessageManager {

    private final CoreConfig coreConfig;

    public MessageManager(CoreConfig coreConfig) {
        this.coreConfig = Objects.requireNonNull(
            coreConfig,
            "CoreConfig cannot be null"
        );
    }

    public String format(String message) {
        Objects.requireNonNull(
            message,
            "Message cannot be null"
        );

        return ChatColor.translateAlternateColorCodes(
            '&',
            message
        );
    }

    public String getPrefix() {
        return format(coreConfig.getPrefix());
    }

    public void send(Player player, String message) {
        Objects.requireNonNull(
            player,
            "Player cannot be null"
        );

        player.sendMessage(format(message));
    }

    public void sendPrefixed(Player player, String message) {
        Objects.requireNonNull(
            player,
            "Player cannot be null"
        );

        player.sendMessage(
            getPrefix() + " " + format(message)
        );
    }

    public void broadcast(String message) {
        Bukkit.broadcastMessage(format(message));
    }

    public void broadcastPrefixed(String message) {
        Bukkit.broadcastMessage(
            getPrefix() + " " + format(message)
        );
    }
}
