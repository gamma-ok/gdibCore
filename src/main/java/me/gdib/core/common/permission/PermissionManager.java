package me.gdib.core.common.permission;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import me.gdib.core.common.config.CoreConfig;

public class PermissionManager {

    private final CoreConfig coreConfig;

    public PermissionManager(CoreConfig coreConfig) {

        if (coreConfig == null) {
            throw new IllegalArgumentException(
                    "CoreConfig cannot be null."
            );
        }

        this.coreConfig = coreConfig;
    }

    public boolean hasPermission(
            CommandSender sender,
            String permission) {

        if (sender == null || permission == null
                || permission.trim().isEmpty()) {

            return false;
        }

        return sender.hasPermission(permission);
    }

    public boolean hasAllPermissions(
            CommandSender sender,
            String... permissions) {

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
            String... permissions) {

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

    public String getPermissionMessage() {
        return coreConfig.getNoPermissionMessage();
    }

    public void sendPermissionMessage(CommandSender sender) {

        if (sender != null) {

            String message = coreConfig.getNoPermissionMessage();

            sender.sendMessage(
                    ChatColor.translateAlternateColorCodes(
                            '&',
                            message
                    )
            );
        }
    }
}
