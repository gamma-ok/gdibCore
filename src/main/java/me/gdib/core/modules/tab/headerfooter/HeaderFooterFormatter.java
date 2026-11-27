package me.gdib.core.modules.tab.headerfooter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.entity.Player;

import me.gdib.core.common.placeholder.PlaceholderManager;
import me.gdib.core.common.text.TextFormatter;
import me.gdib.core.modules.tab.animation.TabPlaceholderResolver;

public final class HeaderFooterFormatter {

    private final TabPlaceholderResolver animationResolver;
    private final PlaceholderManager placeholderManager;
    private final TextFormatter textFormatter;

    public HeaderFooterFormatter(
            TabPlaceholderResolver animationResolver,
            PlaceholderManager placeholderManager,
            TextFormatter textFormatter
    ) {
        this.animationResolver = Objects.requireNonNull(
                animationResolver,
                "Animation resolver cannot be null"
        );

        this.placeholderManager = Objects.requireNonNull(
                placeholderManager,
                "Placeholder manager cannot be null"
        );

        this.textFormatter = Objects.requireNonNull(
                textFormatter,
                "Text formatter cannot be null"
        );
    }

    public List<String> formatLines(Player player, List<String> lines) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(lines, "Lines cannot be null");

        List<String> formattedLines = new ArrayList<>(lines.size());

        for (String line : lines) {
            String formatted = animationResolver.resolve(line);

            formatted = placeholderManager.parse(player, formatted);

            formatted = textFormatter.colorize(formatted);

            formattedLines.add(formatted);
        }

        return List.copyOf(formattedLines);
    }

    public String formatLine(Player player, String line) {
        Objects.requireNonNull(line, "Line cannot be null");

        return formatLines(player, List.of(line)).get(0);
    }
}
