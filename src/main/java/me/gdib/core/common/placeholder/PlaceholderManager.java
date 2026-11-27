package me.gdib.core.common.placeholder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.bukkit.entity.Player;

public final class PlaceholderManager {

    private final Map<String, Function<Player, String>> placeholders;

    public PlaceholderManager() {
        this.placeholders = new LinkedHashMap<>();
    }

    public void register(
            String key,
            Function<Player, String> provider) {

        validateKey(key);

        Objects.requireNonNull(
            provider,
            "Placeholder provider cannot be null"
        );

        if (placeholders.containsKey(key)) {
            throw new IllegalStateException(
                "Placeholder is already registered: " + key
            );
        }

        placeholders.put(key, provider);
    }

    public void unregister(String key) {
        validateKey(key);
        placeholders.remove(key);
    }

    public boolean isRegistered(String key) {
        validateKey(key);
        return placeholders.containsKey(key);
    }

    public String parse(Player player, String text) {
        Objects.requireNonNull(
            text,
            "Text cannot be null"
        );

        String result = text;

        for (Map.Entry<String, Function<Player, String>> entry
                : placeholders.entrySet()) {

            String placeholder = "%" + entry.getKey() + "%";

            if (!result.contains(placeholder)) {
                continue;
            }

            String value = Objects.requireNonNull(
                entry.getValue().apply(player),
                "Placeholder provider returned null: "
                    + entry.getKey()
            );

            result = result.replace(placeholder, value);
        }

        return result;
    }

    public int getPlaceholderCount() {
        return placeholders.size();
    }

    public void clear() {
        placeholders.clear();
    }

    private void validateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Placeholder key cannot be null or empty"
            );
        }

        if (!key.matches("[a-zA-Z0-9_.-]+")) {
            throw new IllegalArgumentException(
                "Invalid placeholder key: " + key
            );
        }
    }
}
