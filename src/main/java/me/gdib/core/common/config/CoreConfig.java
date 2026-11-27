package me.gdib.core.common.config;

import java.util.Objects;

import org.bukkit.configuration.file.FileConfiguration;

public final class CoreConfig {

    private final ConfigManager configManager;
    private FileConfiguration configuration;

    public CoreConfig(ConfigManager configManager) {

        this.configManager = Objects.requireNonNull(
            configManager,
            "ConfigManager cannot be null"
        );

        load();
    }

    public void load() {

        this.configuration = Objects.requireNonNull(
            configManager.loadConfig("config.yml"),
            "Configuration cannot be null"
        );
    }

    public void reload() {

        configManager.reloadConfig("config.yml");

        this.configuration = Objects.requireNonNull(
            configManager.getConfig("config.yml"),
            "Configuration cannot be null"
        );
    }

    public void save() {
        configManager.saveConfig("config.yml");
    }

    public String getPrefix() {

        return configuration.getString(
            "messages.prefix",
            "&b&lGD&f&lIB &8» &7"
        );
    }

    public String getNoPermissionMessage() {

        return configuration.getString(
            "messages.no-permission",
            "&cYou do not have permission to execute this action."
        );
    }

    public boolean isDebugEnabled() {

        return configuration.getBoolean(
            "settings.debug",
            false
        );
    }

    public FileConfiguration getConfiguration() {
        return configuration;
    }
}
