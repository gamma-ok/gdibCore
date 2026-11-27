package me.gdib.core.modules.tab.layout.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import me.gdib.core.modules.tab.config.TabConfig;
import me.gdib.core.modules.tab.layout.model.TabLayoutDirection;
import me.gdib.core.modules.tab.layout.model.TabLayoutGroup;
import me.gdib.core.modules.tab.layout.model.TabLayoutSlot;

public final class TabLayoutConfig {

	private final TabConfig tabConfig;

	private boolean enabled;
	private TabLayoutDirection direction;
	private String defaultSkin;
	private String remainingPlayersText;

	private List<TabLayoutSlot> slots;
	private List<TabLayoutGroup> groups;

	public TabLayoutConfig(TabConfig tabConfig) {
		this.tabConfig = Objects.requireNonNull(tabConfig, "TabConfig cannot be null");

		load();
	}

	public void load() {
		FileConfiguration config = tabConfig.getConfiguration();

		enabled = config.getBoolean("layout.enabled", false);

		direction = TabLayoutDirection.fromString(config.getString("layout.direction", "COLUMNS"));

		defaultSkin = config.getString("layout.default-skin", null);

		remainingPlayersText = config.getString("layout.remaining-players-text", "&7And %remaining% more...");

		slots = loadSlots(config);
		groups = loadGroups(config);
	}

	private List<TabLayoutSlot> loadSlots(FileConfiguration config) {
		List<TabLayoutSlot> result = new ArrayList<>();

		ConfigurationSection section = config.getConfigurationSection("layout.slots");

		if (section == null) {
			return Collections.unmodifiableList(result);
		}

		Set<Integer> usedPositions = new HashSet<>();

		for (String key : section.getKeys(false)) {
			int position;

			try {
				position = Integer.parseInt(key);
			} catch (NumberFormatException exception) {
				throw new IllegalArgumentException("Invalid TAB layout slot: " + key, exception);
			}

			if (!usedPositions.add(position)) {
				throw new IllegalArgumentException("Duplicate TAB layout position: " + position);
			}

			String path = "layout.slots." + key;

			String text = config.getString(path + ".text", "");
			String texture = config.getString(path + ".texture", null);

			result.add(new TabLayoutSlot(position, text, texture));
		}

		return Collections.unmodifiableList(result);
	}

	private List<TabLayoutGroup> loadGroups(FileConfiguration config) {
		List<TabLayoutGroup> result = new ArrayList<>();

		ConfigurationSection section = config.getConfigurationSection("layout.groups");

		if (section == null) {
			return Collections.unmodifiableList(result);
		}

		for (String name : section.getKeys(false)) {
			String path = "layout.groups." + name;

			int startSlot = config.getInt(path + ".start-slot", 1);
			int endSlot = config.getInt(path + ".end-slot", 80);
			int priority = config.getInt(path + ".priority", 0);

			String permission = config.getString(path + ".permission", null);

			result.add(new TabLayoutGroup(name, startSlot, endSlot, priority, permission));
		}

		return Collections.unmodifiableList(result);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public TabLayoutDirection getDirection() {
		return direction;
	}

	public String getDefaultSkin() {
		return defaultSkin;
	}

	public String getRemainingPlayersText() {
		return remainingPlayersText;
	}

	public List<TabLayoutSlot> getSlots() {
		return slots;
	}

	public List<TabLayoutGroup> getGroups() {
		return groups;
	}

	public TabLayoutGroup getGroup(String name) {
		if (name == null) {
			return null;
		}

		for (TabLayoutGroup group : groups) {
			if (group.getName().equalsIgnoreCase(name)) {
				return group;
			}
		}

		return null;
	}

	public TabLayoutSlot getSlot(int position) {
		for (TabLayoutSlot slot : slots) {
			if (slot.getPosition() == position) {
				return slot;
			}
		}

		return null;
	}
}
