package me.gdib.core.modules.tab.headerfooter;

import java.util.Objects;

import org.bukkit.entity.Player;

import me.gdib.core.modules.tab.config.HeaderFooterConfig;

public final class HeaderFooterManager {

    private final HeaderFooterConfig configuration;

    public HeaderFooterManager(HeaderFooterConfig configuration) {
        this.configuration = Objects.requireNonNull(
                configuration,
                "Header/Footer configuration cannot be null"
        );
    }

    public HeaderFooterDesign getDesign(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");

        if (!configuration.isEnabled()) {
            return null;
        }

        for (HeaderFooterDesign design : configuration.getDesigns().values()) {

            if ("default".equalsIgnoreCase(design.getName())) {
                continue;
            }

            if (matchesCondition(player, design)) {
                return design;
            }
        }

        return configuration.getDefaultDesign();
    }

    public boolean hasDesign(Player player) {
        return getDesign(player) != null;
    }

    public boolean isEnabled() {
        return configuration.isEnabled();
    }

    private boolean matchesCondition(
            Player player,
            HeaderFooterDesign design
    ) {
        if (!design.hasDisplayCondition()) {
            return false;
        }

        String condition = design.getDisplayCondition().trim();

        String worldName = player.getWorld().getName();

        if (condition.contains("!=")) {
            String[] parts = condition.split("!=", 2);

            return parts.length == 2
                    && "%world%".equalsIgnoreCase(parts[0].trim())
                    && !worldName.equalsIgnoreCase(parts[1].trim());
        }

        if (condition.contains("=")) {
            String[] parts = condition.split("=", 2);

            return parts.length == 2
                    && "%world%".equalsIgnoreCase(parts[0].trim())
                    && worldName.equalsIgnoreCase(parts[1].trim());
        }

        return false;
    }
}
