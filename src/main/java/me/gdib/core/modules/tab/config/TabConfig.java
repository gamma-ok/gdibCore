package me.gdib.core.modules.tab.config;

import java.util.Objects;

import org.bukkit.configuration.file.FileConfiguration;

import me.gdib.core.kernel.ModuleContext;
import me.gdib.core.kernel.ModuleConfig;

public final class TabConfig {

	private final ModuleConfig moduleConfig;

	private FileConfiguration configuration;

	private boolean enabled;
	private boolean headerFooterEnabled;
	private boolean layoutEnabled;
	private int updateInterval;

	public TabConfig(ModuleContext context) {
		Objects.requireNonNull(context, "Module context cannot be null");

		this.moduleConfig = context.getModuleConfig();

		load();
	}

	public void load() {
		configuration = Objects.requireNonNull(moduleConfig.getConfig(), "TAB configuration cannot be null");

		enabled = configuration.getBoolean("tab.enabled", true);

		headerFooterEnabled = configuration.getBoolean("header-footer.enabled", true);

		layoutEnabled = configuration.getBoolean("layout.enabled", false);

		updateInterval = configuration.getInt("update-interval", 500);

		if (updateInterval < 50) {
			throw new IllegalArgumentException("TAB update interval must be at least 50 milliseconds.");
		}
	}

	public void reload() {
		moduleConfig.reload();
		load();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isHeaderFooterEnabled() {
		return headerFooterEnabled;
	}

	public boolean isLayoutEnabled() {
		return layoutEnabled;
	}

	public int getUpdateInterval() {
		return updateInterval;
	}

	public FileConfiguration getConfiguration() {
		return configuration;
	}
}
