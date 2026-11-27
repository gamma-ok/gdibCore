package me.gdib.core.modules.tab.layout.model;

import java.util.Objects;

public final class TabSkinTexture {

    public enum Source {
        CUSTOM,
        MINESKIN
    }

    private final String value;
    private final String signature;
    private final Source source;

    public TabSkinTexture(
            String value,
            String signature,
            Source source
    ) {
        this.value = Objects.requireNonNull(
                value,
                "Texture value cannot be null"
        ).trim();

        if (this.value.isEmpty()) {
            throw new IllegalArgumentException(
                    "Texture value cannot be empty."
            );
        }

        this.signature = signature == null || signature.isBlank()
                ? null
                : signature.trim();

        this.source = Objects.requireNonNull(
                source,
                "Texture source cannot be null"
        );
    }

    public String getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }

    public Source getSource() {
        return source;
    }

    public boolean hasSignature() {
        return signature != null;
    }

    public boolean isCustom() {
        return source == Source.CUSTOM;
    }

    public boolean isMineSkin() {
        return source == Source.MINESKIN;
    }
}
