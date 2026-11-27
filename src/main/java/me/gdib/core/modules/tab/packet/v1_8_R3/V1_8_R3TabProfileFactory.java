package me.gdib.core.modules.tab.packet.v1_8_R3;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.gdib.core.modules.tab.layout.model.TabSkinTexture;

public final class V1_8_R3TabProfileFactory {

    private static final String PROFILE_PREFIX = "gdib";

    /**
     * Creates a fake GameProfile for a custom TAB entry.
     *
     * @param identifier unique identifier for the entry
     * @return generated GameProfile
     */
    public GameProfile createProfile(String identifier) {

        validateIdentifier(identifier);

        UUID uuid = generateUUID(identifier);

        String profileName = generateProfileName(uuid);

        return new GameProfile(uuid, profileName);
    }

    /**
     * Creates a fake GameProfile containing a custom skin texture.
     *
     * @param identifier unique identifier for the entry
     * @param texture custom skin texture
     * @return generated GameProfile containing the texture
     */
    public GameProfile createProfile(
            String identifier,
            TabSkinTexture texture
    ) {
        Objects.requireNonNull(
                texture,
                "TabSkinTexture cannot be null"
        );

        if (texture.getValue() == null
                || texture.getValue().isBlank()) {
            throw new IllegalArgumentException(
                    "Texture value cannot be empty."
            );
        }

        GameProfile profile = createProfile(identifier);

        if (texture.getSignature() != null
                && !texture.getSignature().isBlank()) {

            profile.getProperties().put(
                    "textures",
                    new Property(
                            "textures",
                            texture.getValue(),
                            texture.getSignature()
                    )
            );

        } else {

            profile.getProperties().put(
                    "textures",
                    new Property(
                            "textures",
                            texture.getValue()
                    )
            );
        }

        return profile;
    }

    /**
     * Generates a deterministic UUID from the identifier.
     */
    private UUID generateUUID(String identifier) {

        String uniqueIdentifier =
                PROFILE_PREFIX + ":" + identifier;

        return UUID.nameUUIDFromBytes(
                uniqueIdentifier.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }

    /**
     * Generates a valid 16-character Minecraft profile name.
     *
     * The name is derived from the UUID to avoid losing
     * its distinguishing suffix when truncating.
     */
    private String generateProfileName(UUID uuid) {

        String uuidHex = uuid.toString()
                .replace("-", "");

        return "g" + uuidHex.substring(0, 15);
    }

    private void validateIdentifier(String identifier) {

        Objects.requireNonNull(
                identifier,
                "Profile identifier cannot be null"
        );

        if (identifier.isBlank()) {
            throw new IllegalArgumentException(
                    "Profile identifier cannot be empty."
            );
        }
    }
}
