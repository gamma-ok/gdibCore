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
        this.configuration = configManager.loadConfig("config.yml");
    }

    public void reload() {
        configManager.reloadConfig("config.yml");
        this.configuration = configManager.getConfig("config.yml");
    }

    public void save() {
        configManager.saveConfig("config.yml");
    }

    public String getPrefix() {
        return configuration.getString(
            "messages.prefix",
            "&b&lGDIB &8» &7"
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
