package me.gdib.core.modules.tab.layout.model;

import java.util.Objects;
import java.util.UUID;

public final class TabLayoutEntry {

    public enum Type {
        PLAYER,
        TEXT,
        CUSTOM_HEAD
    }

    private final int position;
    private final Type type;
    private final UUID playerId;
    private final String text;
    private final String texture;

    private TabLayoutEntry(
            int position,
            Type type,
            UUID playerId,
            String text,
            String texture
    ) {
        if (position < 1 || position > 80) {
            throw new IllegalArgumentException(
                    "Layout position must be between 1 and 80."
            );
        }

        this.position = position;
        this.type = Objects.requireNonNull(
                type,
                "Entry type cannot be null"
        );

        this.playerId = playerId;
        this.text = text;
        this.texture = texture;

        validate();
    }

    private void validate() {
        switch (type) {
            case PLAYER:
                Objects.requireNonNull(
                        playerId,
                        "Player UUID cannot be null"
                );
                break;

            case TEXT:
                if (text == null || text.isBlank()) {
                    throw new IllegalArgumentException(
                            "Text entry cannot be empty."
                    );
                }
                break;

            case CUSTOM_HEAD:
                if (texture == null || texture.isBlank()) {
                    throw new IllegalArgumentException(
                            "Custom head texture cannot be empty."
                    );
                }
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported layout entry type."
                );
        }
    }

    public static TabLayoutEntry player(
            int position,
            UUID playerId
    ) {
        return new TabLayoutEntry(
                position,
                Type.PLAYER,
                playerId,
                null,
                null
        );
    }

    public static TabLayoutEntry text(
            int position,
            String text
    ) {
        return new TabLayoutEntry(
                position,
                Type.TEXT,
                null,
                text,
                null
        );
    }

    public static TabLayoutEntry customHead(
            int position,
            String text,
            String texture
    ) {
        return new TabLayoutEntry(
                position,
                Type.CUSTOM_HEAD,
                null,
                text,
                texture
        );
    }

    public int getPosition() {
        return position;
    }

    public Type getType() {
        return type;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getText() {
        return text;
    }

    public String getTexture() {
        return texture;
    }

    public boolean isPlayer() {
        return type == Type.PLAYER;
    }

    public boolean isText() {
        return type == Type.TEXT;
    }

    public boolean isCustomHead() {
        return type == Type.CUSTOM_HEAD;
    }
}
