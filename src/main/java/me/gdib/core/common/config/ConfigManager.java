package me.gdib.core.common.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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

        fileName = validateFileName(fileName);

        FileConfiguration existing = configurations.get(fileName);

        if (existing != null) {
            return existing;
        }

        File file = new File(dataFolder, fileName);

        if (!file.exists()) {

            createParentDirectories(file);

            try (InputStream resource = plugin.getResource(fileName)) {

                if (resource != null) {
                    Files.copy(resource, file.toPath());
                } else {
                    Files.createFile(file.toPath());
                }

            } catch (IOException exception) {
                throw new IllegalStateException(
                    "Could not create config: " + fileName,
                    exception
                );
            }
        }

        if (!file.isFile()) {
            throw new IllegalStateException(
                "Configuration path is not a file: " + fileName
            );
        }

        FileConfiguration configuration =
            YamlConfiguration.loadConfiguration(file);

        configurations.put(fileName, configuration);

        return configuration;
    }

    public FileConfiguration getConfig(String fileName) {

        fileName = validateFileName(fileName);

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

        fileName = validateFileName(fileName);

        FileConfiguration configuration = getConfig(fileName);

        File file = new File(dataFolder, fileName);

        createParentDirectories(file);

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

        fileName = validateFileName(fileName);

        File file = new File(dataFolder, fileName);

        if (!file.isFile()) {
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

        fileName = validateFileName(fileName);

        configurations.remove(fileName);
    }

    public void unloadAll() {
        configurations.clear();
    }

    public boolean isLoaded(String fileName) {

        fileName = validateFileName(fileName);

        return configurations.containsKey(fileName);
    }

    private String validateFileName(String fileName) {

        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Configuration file name cannot be null or empty"
            );
        }

        String normalized = fileName.trim();

        if (!normalized.toLowerCase().endsWith(".yml")) {
            throw new IllegalArgumentException(
                "Configuration file must have a .yml extension"
            );
        }

        if (normalized.startsWith("/")
                || normalized.contains("\\")
                || normalized.contains(":")) {

            throw new IllegalArgumentException(
                "Invalid configuration file path: " + normalized
            );
        }

        String[] segments = normalized.split("/", -1);

        for (String segment : segments) {

            if (segment.isEmpty()
                    || segment.equals(".")
                    || segment.equals("..")) {

                throw new IllegalArgumentException(
                    "Invalid configuration file path: " + normalized
                );
            }
        }

        try {

            File baseDirectory = dataFolder.getCanonicalFile();
            File targetFile = new File(dataFolder, normalized)
                .getCanonicalFile();

            if (!targetFile.toPath().startsWith(
                    baseDirectory.toPath())) {

                throw new IllegalArgumentException(
                    "Configuration path is outside plugin directory"
                );
            }

        } catch (IOException exception) {
            throw new IllegalArgumentException(
                "Could not validate configuration path: " + normalized,
                exception
            );
        }

        return normalized;
    }

    private void createParentDirectories(File file) {

        File parent = file.getParentFile();

        if (parent != null
                && !parent.exists()
                && !parent.mkdirs()) {

            throw new IllegalStateException(
                "Could not create configuration directory: "
                + parent.getPath()
            );
        }

        if (parent != null && !parent.isDirectory()) {
            throw new IllegalStateException(
                "Configuration parent is not a directory: "
                + parent.getPath()
            );
        }
    }
}
