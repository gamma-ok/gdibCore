package me.gdib.core.modules.tab.animation;

import java.util.List;
import java.util.Objects;

public final class TabAnimation {

    private final String name;
    private final long changeInterval;
    private final List<String> texts;

    public TabAnimation(String name, long changeInterval, List<String> texts) {
        Objects.requireNonNull(name, "Animation name cannot be null");
        Objects.requireNonNull(texts, "Animation texts cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Animation name cannot be empty."
            );
        }

        if (changeInterval < 50) {
            throw new IllegalArgumentException(
                    "Animation interval must be at least 50 milliseconds."
            );
        }

        if (texts.isEmpty()) {
            throw new IllegalArgumentException(
                    "Animation must contain at least one text."
            );
        }

        if (texts.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "Animation texts cannot contain null values."
            );
        }

        this.name = name;
        this.changeInterval = changeInterval;
        this.texts = List.copyOf(texts);
    }

    public String getName() {
        return name;
    }

    public long getChangeInterval() {
        return changeInterval;
    }

    public List<String> getTexts() {
        return texts;
    }

    public int getTextCount() {
        return texts.size();
    }
}
