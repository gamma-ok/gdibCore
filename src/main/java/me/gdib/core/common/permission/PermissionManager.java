package me.gdib.core.common.permission;

import java.util.Objects;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import me.gdib.core.common.config.CoreConfig;

public final class PermissionManager {

    private final CoreConfig coreConfig;

    public PermissionManager(CoreConfig coreConfig) {
        this.coreConfig = Objects.requireNonNull(
                coreConfig,
                "CoreConfig cannot be null"
        );
    }

    // =========================================================
    // PERMISSION CHECKS
    // =========================================================

    public boolean hasPermission(
            CommandSender sender,
            String permission
    ) {
        if (sender == null || permission == null
                || permission.trim().isEmpty()) {
            return false;
        }

        return sender.hasPermission(permission);
    }

    public boolean hasAllPermissions(
            CommandSender sender,
            String... permissions
    ) {
        if (sender == null || permissions == null) {
            return false;
        }

        for (String permission : permissions) {
            if (!hasPermission(sender, permission)) {
                return false;
            }
        }

        return true;
    }

    public boolean hasAnyPermission(
            CommandSender sender,
            String... permissions
    ) {
        if (sender == null || permissions == null) {
            return false;
        }

        for (String permission : permissions) {
            if (hasPermission(sender, permission)) {
                return true;
            }
        }

        return false;
    }

    // =========================================================
    // PERMISSION MESSAGES
    // =========================================================

    public String getPermissionMessage() {
        return coreConfig.getNoPermissionMessage();
    }

    public void sendPermissionMessage(CommandSender sender) {
        if (sender == null) {
            return;
        }

        String message = getPermissionMessage();

        sender.sendMessage(
                ChatColor.translateAlternateColorCodes('&', message)
        );
    }
}
