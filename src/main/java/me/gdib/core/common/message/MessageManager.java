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

    // =========================================================
    // MESSAGE FORMATTING
    // =========================================================

    public String format(String message) {
        Objects.requireNonNull(message, "Message cannot be null");

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getPrefix() {
        return format(coreConfig.getPrefix());
    }

    // =========================================================
    // PLAYER MESSAGES
    // =========================================================

    public void send(Player player, String message) {
        ensureMainThread();

        Objects.requireNonNull(player, "Player cannot be null");

        player.sendMessage(format(message));
    }

    public void sendPrefixed(Player player, String message) {
        ensureMainThread();

        Objects.requireNonNull(player, "Player cannot be null");

        player.sendMessage(
                getPrefix() + " " + format(message)
        );
    }

    // =========================================================
    // BROADCAST MESSAGES
    // =========================================================

    public void broadcast(String message) {
        ensureMainThread();

        Bukkit.broadcastMessage(format(message));
    }

    public void broadcastPrefixed(String message) {
        ensureMainThread();

        Bukkit.broadcastMessage(
                getPrefix() + " " + format(message)
        );
    }

    // =========================================================
    // THREAD VALIDATION
    // =========================================================

    private void ensureMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException(
                    "MessageManager operations must be executed "
                            + "on the main server thread."
            );
        }
    }
}
