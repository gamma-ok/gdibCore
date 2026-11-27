package me.gdib.core.modules.tab.skin;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import me.gdib.core.modules.tab.layout.TabSkinTextureManager;
import me.gdib.core.modules.tab.layout.model.TabSkinTexture;

public final class TabSkinTextureService {

	private final TabSkinTextureManager textureManager;
	private final Optional<MineSkinService> mineSkinService;

	public TabSkinTextureService(TabSkinTextureManager textureManager, MineSkinService mineSkinService) {
		this.textureManager = Objects.requireNonNull(textureManager, "TabSkinTextureManager cannot be null");

		this.mineSkinService = Optional.ofNullable(mineSkinService);
	}

	/**
	 * Retrieves a previously registered texture.
	 */
	public TabSkinTexture getTexture(String identifier) {
		validateIdentifier(identifier);

		return textureManager.get(identifier);
	}

	/**
	 * Checks whether a texture is registered.
	 */
	public boolean hasTexture(String identifier) {
		validateIdentifier(identifier);

		return textureManager.contains(identifier);
	}

	/**
	 * Retrieves a cached texture or requests its generation through MineSkin when
	 * it is not available.
	 */
	public CompletableFuture<TabSkinTexture> requestTexture(String identifier, String skinUrl) {
		validateIdentifier(identifier);

		if (skinUrl == null || skinUrl.isBlank()) {
			throw new IllegalArgumentException("Skin URL cannot be empty.");
		}

		// Reuse an existing texture whenever possible.
		TabSkinTexture cached = textureManager.get(identifier);

		if (cached != null) {
			return CompletableFuture.completedFuture(cached);
		}

		// Check whether MineSkin integration is available.
		if (mineSkinService.isEmpty()) {
			return CompletableFuture.failedFuture(new IllegalStateException("MineSkin integration is not available."));
		}

		return mineSkinService.get().requestTexture(identifier, skinUrl);
	}

	/**
	 * Returns whether MineSkin integration is available.
	 */
	public boolean isMineSkinAvailable() {
		return mineSkinService.isPresent() && !mineSkinService.get().isClosed();
	}

	/**
	 * Returns the total number of registered textures.
	 */
	public int getTextureCount() {
		return textureManager.getTextureCount();
	}

	private void validateIdentifier(String identifier) {
		Objects.requireNonNull(identifier, "Texture identifier cannot be null");

		if (identifier.isBlank()) {
			throw new IllegalArgumentException("Texture identifier cannot be empty.");
		}
	}
}
