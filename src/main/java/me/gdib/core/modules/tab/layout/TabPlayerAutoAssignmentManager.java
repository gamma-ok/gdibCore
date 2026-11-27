package me.gdib.core.modules.tab.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import me.gdib.core.modules.tab.layout.model.TabPlayerAssignment;

public final class TabPlayerAutoAssignmentManager {

    private static final int MAX_SLOTS = 80;

    private final TabLayoutManager layoutManager;
    private final TabPlayerAssignmentManager assignmentManager;

    public TabPlayerAutoAssignmentManager(
            TabLayoutManager layoutManager,
            TabPlayerAssignmentManager assignmentManager
    ) {
        this.layoutManager = Objects.requireNonNull(
                layoutManager,
                "TabLayoutManager cannot be null"
        );

        this.assignmentManager = Objects.requireNonNull(
                assignmentManager,
                "TabPlayerAssignmentManager cannot be null"
        );
    }

    public Map<Integer, UUID> calculateAssignments(
            Collection<UUID> onlinePlayers
    ) {
        Objects.requireNonNull(
                onlinePlayers,
                "Online players cannot be null"
        );

        Map<Integer, UUID> result = new LinkedHashMap<>();

        if (!layoutManager.isEnabled()) {
            return Collections.unmodifiableMap(result);
        }

        Set<UUID> onlinePlayerIds = new LinkedHashSet<>();

        for (UUID playerId : onlinePlayers) {
            if (playerId != null) {
                onlinePlayerIds.add(playerId);
            }
        }

        // Include online players with manual assignments.
        for (TabPlayerAssignment assignment :
                assignmentManager.getAssignments().values()) {

            UUID playerId = assignment.getPlayerId();

            if (onlinePlayerIds.contains(playerId)) {
                result.put(assignment.getSlot(), playerId);
            }
        }

        // Identify players who still need an automatic position.
        List<UUID> unassignedPlayers = new ArrayList<>();

        for (UUID playerId : onlinePlayerIds) {
            if (!assignmentManager.isPlayerAssigned(playerId)) {
                unassignedPlayers.add(playerId);
            }
        }

        // Fill available positions without overriding reserved slots.
        int nextPlayerIndex = 0;

        for (int slot = 1;
                slot <= MAX_SLOTS && nextPlayerIndex < unassignedPlayers.size();
                slot++) {

            if (layoutManager.hasSlot(slot)) {
                continue;
            }

            if (assignmentManager.isSlotOccupied(slot)) {
                continue;
            }

            UUID playerId = unassignedPlayers.get(nextPlayerIndex);

            result.put(slot, playerId);
            nextPlayerIndex++;
        }

        return Collections.unmodifiableMap(result);
    }

    public int getUnassignedPlayerCount(
            Collection<UUID> onlinePlayers
    ) {
        Objects.requireNonNull(
                onlinePlayers,
                "Online players cannot be null"
        );

        Map<Integer, UUID> assignments =
                calculateAssignments(onlinePlayers);

        return Math.max(0, onlinePlayers.size() - assignments.size());
    }
}
