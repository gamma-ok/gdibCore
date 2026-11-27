package me.gdib.core.modules.tab.layout.model;

import java.util.Objects;
import java.util.UUID;

public final class TabPlayerAssignment {

    private final UUID playerId;
    private final int slot;

    public TabPlayerAssignment(UUID playerId, int slot) {
        this.playerId = Objects.requireNonNull(
                playerId,
                "Player UUID cannot be null"
        );

        if (slot < 1 || slot > 80) {
            throw new IllegalArgumentException(
                    "Player slot must be between 1 and 80."
            );
        }

        this.slot = slot;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getSlot() {
        return slot;
    }
}
