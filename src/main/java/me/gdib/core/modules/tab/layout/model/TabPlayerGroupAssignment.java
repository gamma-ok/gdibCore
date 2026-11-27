package me.gdib.core.modules.tab.layout.model;

import java.util.Objects;
import java.util.UUID;

public final class TabPlayerGroupAssignment {

    private final UUID playerId;
    private final String groupName;

    public TabPlayerGroupAssignment(UUID playerId, String groupName) {
        this.playerId = Objects.requireNonNull(
                playerId,
                "Player UUID cannot be null"
        );

        this.groupName = Objects.requireNonNull(
                groupName,
                "Group name cannot be null"
        ).trim();

        if (this.groupName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Group name cannot be empty."
            );
        }
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getGroupName() {
        return groupName;
    }
}
