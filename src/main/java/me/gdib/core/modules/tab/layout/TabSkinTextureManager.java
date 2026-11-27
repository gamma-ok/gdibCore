package me.gdib.core.modules.tab.layout;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.util.Locale;
import java.util.Objects;

import me.gdib.core.modules.tab.layout.model.TabSkinTexture;

public final class TabSkinTextureManager {

    private final ConcurrentMap<String, TabSkinTexture> textures;

    public TabSkinTextureManager() {
        this.textures = new ConcurrentHashMap<>();
    }

    public boolean register(
            String identifier,
            TabSkinTexture texture
    ) {
        String key = normalizeIdentifier(identifier);

        Objects.requireNonNull(
                texture,
                "Texture cannot be null"
        );

        return textures.putIfAbsent(key, texture) == null;
    }

    public void registerOrReplace(
            String identifier,
            TabSkinTexture texture
    ) {
        String key = normalizeIdentifier(identifier);

        Objects.requireNonNull(
                texture,
                "Texture cannot be null"
        );

        textures.put(key, texture);
    }

    public TabSkinTexture get(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return null;
        }

        return textures.get(
                identifier.trim().toLowerCase(Locale.ROOT)
        );
    }

    public boolean unregister(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return false;
        }

        return textures.remove(
                identifier.trim().toLowerCase(Locale.ROOT)
        ) != null;
    }

    public boolean contains(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return false;
        }

        return textures.containsKey(
                identifier.trim().toLowerCase(Locale.ROOT)
        );
    }

    public Map<String, TabSkinTexture> getTextures() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<>(textures)
        );
    }

    public int getTextureCount() {
        return textures.size();
    }

    public void clear() {
        textures.clear();
    }

    private String normalizeIdentifier(String identifier) {
        Objects.requireNonNull(
                identifier,
                "Texture identifier cannot be null"
        );

        String normalized = identifier.trim().toLowerCase(Locale.ROOT);

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "Texture identifier cannot be empty."
            );
        }

        return normalized;
    }
}
