package me.gdib.core.modules.tab.skin;

import java.util.Objects;

import org.bukkit.configuration.file.FileConfiguration;

import me.gdib.core.modules.tab.config.TabConfig;

public final class MineSkinConfig {

	private final TabConfig tabConfig;

	private boolean enabled;
	private String apiKey;
	private int pollIntervalSeconds;
	private int maxPollAttempts;

	public MineSkinConfig(TabConfig tabConfig) {
		this.tabConfig = Objects.requireNonNull(tabConfig, "TabConfig cannot be null");

		load();
	}

	public void load() {
		FileConfiguration config = tabConfig.getConfiguration();

		boolean newEnabled = config.getBoolean("mineskin.enabled", false);

		String newApiKey = config.getString("mineskin.api-key", "");

		int newPollInterval = config.getInt("mineskin.poll-interval-seconds", 2);

		int newMaxAttempts = config.getInt("mineskin.max-poll-attempts", 30);

		if (newPollInterval < 1) {
			throw new IllegalArgumentException("MineSkin poll interval must be at least 1 second.");
		}

		if (newMaxAttempts < 1) {
			throw new IllegalArgumentException("MineSkin max poll attempts must be at least 1.");
		}

		// Apply validated configuration.
		this.enabled = newEnabled;
		this.apiKey = newApiKey == null ? "" : newApiKey.trim();
		this.pollIntervalSeconds = newPollInterval;
		this.maxPollAttempts = newMaxAttempts;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getApiKey() {
		return apiKey;
	}

	public boolean hasApiKey() {
		return !apiKey.isEmpty();
	}

	public int getPollIntervalSeconds() {
		return pollIntervalSeconds;
	}

	public int getMaxPollAttempts() {
		return maxPollAttempts;
	}
}
