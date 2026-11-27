package me.gdib.core.common.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigManager {

    private final JavaPlugin plugin;
    private final File dataFolder;

    private final Map<String, FileConfiguration> configurations;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(
            plugin,
            "Plugin cannot be null"
        );

        this.dataFolder = plugin.getDataFolder();
        this.configurations = new HashMap<>();

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IllegalStateException(
                "Could not create plugin data folder"
            );
        }
    }

    public FileConfiguration loadConfig(String fileName) {
        validateFileName(fileName);

        FileConfiguration existing = configurations.get(fileName);

        if (existing != null) {
            return existing;
        }

        File file = new File(dataFolder, fileName);

        if (!file.exists()) {
            String resource = plugin.getResource(fileName) != null
                ? fileName
                : null;

            if (resource != null) {
                plugin.saveResource(resource, false);
            } else {
                try {
                    if (!file.createNewFile()) {
                        throw new IOException(
                            "Could not create configuration file"
                        );
                    }
                } catch (IOException exception) {
                    throw new IllegalStateException(
                        "Could not create config: " + fileName,
                        exception
                    );
                }
            }
        }

        FileConfiguration configuration =
            YamlConfiguration.loadConfiguration(file);

        configurations.put(fileName, configuration);

        return configuration;
    }

    public FileConfiguration getConfig(String fileName) {
        validateFileName(fileName);

        FileConfiguration configuration =
            configurations.get(fileName);

        if (configuration == null) {
            throw new IllegalStateException(
                "Configuration is not loaded: " + fileName
            );
        }

        return configuration;
    }

    public void saveConfig(String fileName) {
        validateFileName(fileName);

        FileConfiguration configuration =
            getConfig(fileName);

        File file = new File(dataFolder, fileName);

        try {
            configuration.save(file);
        } catch (IOException exception) {
            throw new IllegalStateException(
                "Could not save config: " + fileName,
                exception
            );
        }
    }

    public void reloadConfig(String fileName) {
        validateFileName(fileName);

        File file = new File(dataFolder, fileName);

        if (!file.exists()) {
            throw new IllegalStateException(
                "Configuration file does not exist: " + fileName
            );
        }

        FileConfiguration configuration =
            YamlConfiguration.loadConfiguration(file);

        configurations.put(fileName, configuration);
    }

    public void saveAll() {
        for (String fileName : configurations.keySet()) {
            saveConfig(fileName);
        }
    }

    public void reloadAll() {
        for (String fileName : configurations.keySet()) {
            reloadConfig(fileName);
        }
    }

    public void unloadConfig(String fileName) {
        validateFileName(fileName);
        configurations.remove(fileName);
    }

    public void unloadAll() {
        configurations.clear();
    }

    public boolean isLoaded(String fileName) {
        validateFileName(fileName);
        return configurations.containsKey(fileName);
    }

    private void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Configuration file name cannot be null or empty"
            );
        }

        if (!fileName.toLowerCase().endsWith(".yml")) {
            throw new IllegalArgumentException(
                "Configuration file must have a .yml extension"
            );
        }

        if (fileName.contains("/")
                || fileName.contains("\\")
                || fileName.equals(".yml")
                || fileName.contains("..")) {

            throw new IllegalArgumentException(
                "Invalid configuration file name: " + fileName
            );
        }
    }
}
