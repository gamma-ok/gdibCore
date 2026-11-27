package me.gdib.core.modules.tab.layout;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import me.gdib.core.modules.tab.layout.model.TabPlayerAssignment;

public final class TabPlayerAssignmentManager {

    private final Map<UUID, TabPlayerAssignment> assignments;
    private final Map<Integer, UUID> occupiedSlots;

    public TabPlayerAssignmentManager() {
        this.assignments = new LinkedHashMap<>();
        this.occupiedSlots = new LinkedHashMap<>();
    }

    public boolean assign(UUID playerId, int slot) {
        if (playerId == null) {
            throw new IllegalArgumentException(
                    "Player UUID cannot be null."
            );
        }

        // Validate the requested position.
        TabPlayerAssignment assignment =
                new TabPlayerAssignment(playerId, slot);

        // Check whether another player occupies this position.
        UUID occupant = occupiedSlots.get(slot);

        if (occupant != null && !occupant.equals(playerId)) {
            return false;
        }

        // Remove the player's previous assignment, if any.
        TabPlayerAssignment previous = assignments.get(playerId);

        if (previous != null) {
            occupiedSlots.remove(previous.getSlot());
        }

        // Register the new assignment.
        assignments.put(playerId, assignment);
        occupiedSlots.put(slot, playerId);

        return true;
    }

    public boolean unassign(UUID playerId) {
        if (playerId == null) {
            return false;
        }

        TabPlayerAssignment assignment = assignments.remove(playerId);

        if (assignment == null) {
            return false;
        }

        occupiedSlots.remove(assignment.getSlot());

        return true;
    }

    public boolean unassignSlot(int slot) {
        UUID playerId = occupiedSlots.remove(slot);

        if (playerId == null) {
            return false;
        }

        assignments.remove(playerId);

        return true;
    }

    public TabPlayerAssignment getAssignment(UUID playerId) {
        if (playerId == null) {
            return null;
        }

        return assignments.get(playerId);
    }

    public UUID getPlayerAtSlot(int slot) {
        return occupiedSlots.get(slot);
    }

    public boolean isPlayerAssigned(UUID playerId) {
        return playerId != null && assignments.containsKey(playerId);
    }

    public boolean isSlotOccupied(int slot) {
        return occupiedSlots.containsKey(slot);
    }

    public Map<UUID, TabPlayerAssignment> getAssignments() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<>(assignments)
        );
    }

    public int getAssignmentCount() {
        return assignments.size();
    }

    public void clear() {
        assignments.clear();
        occupiedSlots.clear();
    }
}
