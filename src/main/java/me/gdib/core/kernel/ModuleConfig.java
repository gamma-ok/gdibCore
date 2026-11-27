package me.gdib.core.kernel;

import java.util.Objects;

import org.bukkit.configuration.file.FileConfiguration;

import me.gdib.core.common.config.ConfigManager;

public final class ModuleConfig {

	private final String moduleName;
	private final ConfigManager configManager;
	private final String fileName;

	public ModuleConfig(String moduleName, ConfigManager configManager) {

		this.moduleName = Objects.requireNonNull(moduleName, "Module name cannot be null");

		this.configManager = Objects.requireNonNull(configManager, "ConfigManager cannot be null");

		this.fileName = "modules/" + moduleName.toLowerCase() + "/config.yml";
	}

	public FileConfiguration getConfig() {
		return configManager.loadConfig(fileName);
	}

	public void save() {
		configManager.saveConfig(fileName);
	}

	public void reload() {
		configManager.reloadConfig(fileName);
	}

	public String getModuleName() {
		return moduleName;
	}

	public String getFileName() {
		return fileName;
	}

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
