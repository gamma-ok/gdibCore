package me.gdib.core.modules.tab.layout;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import me.gdib.core.modules.tab.layout.model.TabPlayerGroupAssignment;

public final class TabPlayerGroupAssignmentManager {

    private final TabLayoutManager layoutManager;

    private final Map<UUID, TabPlayerGroupAssignment> assignments;

    public TabPlayerGroupAssignmentManager(
            TabLayoutManager layoutManager
    ) {
        this.layoutManager = Objects.requireNonNull(
                layoutManager,
                "TabLayoutManager cannot be null"
        );

        this.assignments = new LinkedHashMap<>();
    }

    public boolean assign(UUID playerId, String groupName) {
        if (playerId == null || groupName == null) {
            return false;
        }

        String normalizedName = groupName.trim();

        if (normalizedName.isEmpty()) {
            return false;
        }

        // Verify that the group exists.
        if (!layoutManager.hasGroup(normalizedName)) {
            return false;
        }

        TabPlayerGroupAssignment assignment =
                new TabPlayerGroupAssignment(
                        playerId,
                        normalizedName
                );

        // Register or replace the player's manual assignment.
        assignments.put(playerId, assignment);

        return true;
    }

    public boolean unassign(UUID playerId) {
        if (playerId == null) {
            return false;
        }

        return assignments.remove(playerId) != null;
    }

    public TabPlayerGroupAssignment getAssignment(UUID playerId) {
        if (playerId == null) {
            return null;
        }

        return assignments.get(playerId);
    }

    public String getAssignedGroup(UUID playerId) {
        TabPlayerGroupAssignment assignment =
                getAssignment(playerId);

        return assignment == null
                ? null
                : assignment.getGroupName();
    }

    public boolean hasAssignment(UUID playerId) {
        return playerId != null
                && assignments.containsKey(playerId);
    }

    public Map<UUID, TabPlayerGroupAssignment> getAssignments() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<>(assignments)
        );
    }

    public int getAssignmentCount() {
        return assignments.size();
    }

    public void clear() {
        assignments.clear();
    }
}
