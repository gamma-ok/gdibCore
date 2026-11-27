package me.gdib.core.modules.tab.layout.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import me.gdib.core.modules.tab.config.TabConfig;
import me.gdib.core.modules.tab.layout.TabSkinTextureManager;
import me.gdib.core.modules.tab.layout.model.TabSkinTexture;

public final class TabSkinTextureLoader {

	private final TabConfig tabConfig;
	private final TabSkinTextureManager textureManager;
	private final Set<String> loadedIdentifiers;

	public TabSkinTextureLoader(TabConfig tabConfig, TabSkinTextureManager textureManager) {
		this.tabConfig = Objects.requireNonNull(tabConfig, "TabConfig cannot be null");

		this.textureManager = Objects.requireNonNull(textureManager, "TabSkinTextureManager cannot be null");

		this.loadedIdentifiers = new LinkedHashSet<>();
	}

	public void load() {

		FileConfiguration config = tabConfig.getConfiguration();

		ConfigurationSection section = config.getConfigurationSection("layout.textures");

		Map<String, TabSkinTexture> texturesToRegister = new LinkedHashMap<>();

		if (section != null) {

			// Validate and collect all textures before modifying the manager.
			for (String identifier : section.getKeys(false)) {

				String value = section.getString(identifier + ".value");
				String signature = section.getString(identifier + ".signature");

				if (value == null || value.isBlank()) {
					throw new IllegalArgumentException(
							"Missing texture value at: layout.textures." + identifier + ".value");
				}

				TabSkinTexture texture = new TabSkinTexture(value, signature, TabSkinTexture.Source.CUSTOM);

				texturesToRegister.put(identifier, texture);
			}
		}

		// Remove textures previously loaded from this configuration.
		unregisterPreviousTextures();

		// Register the validated textures.
		for (Map.Entry<String, TabSkinTexture> entry : texturesToRegister.entrySet()) {

			textureManager.registerOrReplace(entry.getKey(), entry.getValue());

			loadedIdentifiers.add(entry.getKey());
		}
	}

	private void unregisterPreviousTextures() {

		for (String identifier : loadedIdentifiers) {
			textureManager.unregister(identifier);
		}

		loadedIdentifiers.clear();
	}

	public Set<String> getLoadedIdentifiers() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(loadedIdentifiers));
	}

	public int getLoadedTextureCount() {
		return loadedIdentifiers.size();
	}

	public void unload() {
		unregisterPreviousTextures();
	}
}
