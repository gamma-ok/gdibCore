package me.gdib.core.modules.tab.layout.model;

import java.util.Objects;

public final class TabLayoutSlot {

    private final int position;
    private final String text;
    private final String texture;

    public TabLayoutSlot(
            int position,
            String text,
            String texture
    ) {
        if (position < 1 || position > 80) {
            throw new IllegalArgumentException(
                    "TAB slot position must be between 1 and 80."
            );
        }

        this.position = position;

        this.text = Objects.requireNonNull(
                text,
                "Slot text cannot be null"
        );

        this.texture = texture;
    }

    public int getPosition() {
        return position;
    }

    public String getText() {
        return text;
    }

    public String getTexture() {
        return texture;
    }

    public boolean hasTexture() {
        return texture != null && !texture.isBlank();
    }

    public boolean hasText() {
        return !text.isBlank();
    }
}
