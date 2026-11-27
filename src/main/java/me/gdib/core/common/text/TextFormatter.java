package me.gdib.core.common.text;

import java.util.Map;
import java.util.Objects;

import org.bukkit.ChatColor;

public final class TextFormatter {

    public String colorize(String text) {
        Objects.requireNonNull(
            text,
            "Text cannot be null"
        );

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public String stripColors(String text) {
        Objects.requireNonNull(
            text,
            "Text cannot be null"
        );

        return ChatColor.stripColor(
            colorize(text)
        );
    }

    public String replace(
            String text,
            Map<String, String> replacements) {

        Objects.requireNonNull(
            text,
            "Text cannot be null"
        );

        Objects.requireNonNull(
            replacements,
            "Replacements cannot be null"
        );

        String result = text;

        for (Map.Entry<String, String> entry
                : replacements.entrySet()) {

            String placeholder = Objects.requireNonNull(
                entry.getKey(),
                "Placeholder cannot be null"
            );

            String value = Objects.requireNonNull(
                entry.getValue(),
                "Replacement value cannot be null"
            );

            result = result.replace(placeholder, value);
        }

        return result;
    }

    public String format(String text) {
        return colorize(text);
    }

    public String format(
            String text,
            Map<String, String> replacements) {

        return colorize(
            replace(text, replacements)
        );
    }
}
