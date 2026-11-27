package me.gdib.core.common.message;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class MessageManager {

    private final String prefix;

    public MessageManager(String prefix) {
        this.prefix = format(
            Objects.requireNonNull(
                prefix,
                "Prefix cannot be null"
            )
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
        return prefix;
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

        player.sendMessage(prefix + " " + format(message));
    }

    public void broadcast(String message) {
        Bukkit.broadcastMessage(format(message));
    }

    public void broadcastPrefixed(String message) {
        Bukkit.broadcastMessage(
            prefix + " " + format(message)
        );
    }
}
