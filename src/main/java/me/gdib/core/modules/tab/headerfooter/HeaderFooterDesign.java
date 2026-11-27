package me.gdib.core.modules.tab.headerfooter;

import java.util.List;
import java.util.Objects;

public final class HeaderFooterDesign {

    private final String name;
    private final String displayCondition;
    private final List<String> header;
    private final List<String> footer;

    public HeaderFooterDesign(
            String name,
            String displayCondition,
            List<String> header,
            List<String> footer
    ) {
        Objects.requireNonNull(name, "Design name cannot be null");
        Objects.requireNonNull(header, "Header cannot be null");
        Objects.requireNonNull(footer, "Footer cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Design name cannot be empty."
            );
        }

        if (displayCondition != null && displayCondition.isBlank()) {
            throw new IllegalArgumentException(
                    "Display condition cannot be empty."
            );
        }

        if (header.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "Header cannot contain null values."
            );
        }

        if (footer.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "Footer cannot contain null values."
            );
        }

        this.name = name;
        this.displayCondition = displayCondition;
        this.header = List.copyOf(header);
        this.footer = List.copyOf(footer);
    }

    public String getName() {
        return name;
    }

    public String getDisplayCondition() {
        return displayCondition;
    }

    public boolean hasDisplayCondition() {
        return displayCondition != null;
    }

    public List<String> getHeader() {
        return header;
    }

    public List<String> getFooter() {
        return footer;
    }
}
