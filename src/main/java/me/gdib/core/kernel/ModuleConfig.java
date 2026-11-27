package me.gdib.core.kernel;

import java.util.Locale;
import java.util.Objects;

import org.bukkit.configuration.file.FileConfiguration;

import me.gdib.core.common.config.ConfigManager;

public final class ModuleConfig {

    private final String moduleName;
    private final ConfigManager configManager;
    private final String fileName;

    public ModuleConfig(String moduleName, ConfigManager configManager) {

        Objects.requireNonNull(
                moduleName,
                "Module name cannot be null"
        );

        this.configManager = Objects.requireNonNull(
                configManager,
                "ConfigManager cannot be null"
        );

        String validatedName = moduleName.trim();

        if (!validatedName.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException(
                    "Invalid module name: " + moduleName
            );
        }

        this.moduleName = validatedName;

        this.fileName = "modules/"
                + validatedName.toLowerCase(Locale.ROOT)
                + "/config.yml";
    }

    // =========================================================
    // CONFIGURATION ACCESS
    // =========================================================

    public FileConfiguration getConfig() {
        return configManager.loadConfig(fileName);
    }

    public void save() {
        configManager.saveConfig(fileName);
    }

    public void reload() {
        configManager.reloadConfig(fileName);
    }

    // =========================================================
    // MODULE INFORMATION
    // =========================================================

    public String getModuleName() {
        return moduleName;
    }

    public String getFileName() {
        return fileName;
    }

    // =========================================================
    // CONFIGURATION CLEANUP
    // =========================================================

    public void unload() {

        if (!configManager.isLoaded(fileName)) {
            return;
        }

        // Save the configuration before unloading it.
        configManager.saveConfig(fileName);

        // Remove the configuration from memory.
        configManager.unloadConfig(fileName);
    }
}
