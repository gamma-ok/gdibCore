package me.gdib.core.modules.tab.layout;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.gdib.core.modules.tab.layout.model.TabLayoutGroup;
import me.gdib.core.modules.tab.layout.model.TabPlayerAssignment;

public final class TabPlayerGroupAutoAssignmentManager {

    private final TabLayoutManager layoutManager;
    private final TabPlayerAssignmentManager assignmentManager;
    private final TabPlayerGroupResolver groupResolver;

    public TabPlayerGroupAutoAssignmentManager(
            TabLayoutManager layoutManager,
            TabPlayerAssignmentManager assignmentManager,
            TabPlayerGroupResolver groupResolver
    ) {
        this.layoutManager = Objects.requireNonNull(
                layoutManager,
                "TabLayoutManager cannot be null"
        );

        this.assignmentManager = Objects.requireNonNull(
                assignmentManager,
                "TabPlayerAssignmentManager cannot be null"
        );

        this.groupResolver = Objects.requireNonNull(
                groupResolver,
                "TabPlayerGroupResolver cannot be null"
        );
    }

    public Map<Integer, UUID> calculateAssignments(
            Collection<? extends Player> onlinePlayers
    ) {
        Objects.requireNonNull(
                onlinePlayers,
                "Online players cannot be null"
        );

        Map<Integer, UUID> result = new LinkedHashMap<>();

        if (!layoutManager.isEnabled()) {
            return Collections.unmodifiableMap(result);
        }

        Set<Player> uniquePlayers = new LinkedHashSet<>();

        for (Player player : onlinePlayers) {
            if (player != null && player.isOnline()) {
                uniquePlayers.add(player);
            }
        }

        Set<UUID> manuallyAssignedPlayers = new LinkedHashSet<>();

        // 1. Preserve manual position assignments.
        for (TabPlayerAssignment assignment :
                assignmentManager.getAssignments().values()) {

            UUID playerId = assignment.getPlayerId();

            if (!isOnline(uniquePlayers, playerId)) {
                continue;
            }

            result.put(assignment.getSlot(), playerId);
            manuallyAssignedPlayers.add(playerId);
        }

        // 2. Resolve groups and assign remaining players.
        for (Player player : uniquePlayers) {
            UUID playerId = player.getUniqueId();

            if (manuallyAssignedPlayers.contains(playerId)) {
                continue;
            }

            TabLayoutGroup group = groupResolver.resolve(player);

            if (group == null) {
                continue;
            }

            // 3. Find an available position inside the group range.
            for (int slot = group.getStartSlot();
                    slot <= group.getEndSlot();
                    slot++) {

                if (layoutManager.hasSlot(slot)) {
                    continue;
                }

                if (assignmentManager.isSlotOccupied(slot)) {
                    continue;
                }

                if (result.containsKey(slot)) {
                    continue;
                }

                result.put(slot, playerId);
                break;
            }
        }

        return Collections.unmodifiableMap(result);
    }

    private boolean isOnline(
            Set<Player> onlinePlayers,
            UUID playerId
    ) {
        for (Player player : onlinePlayers) {
            if (player.getUniqueId().equals(playerId)) {
                return true;
            }
        }

        return false;
    }

    public int getUnassignedPlayerCount(
            Collection<? extends Player> onlinePlayers
    ) {
        Objects.requireNonNull(
                onlinePlayers,
                "Online players cannot be null"
        );

        int onlineCount = 0;

        for (Player player : onlinePlayers) {
            if (player != null && player.isOnline()) {
                onlineCount++;
            }
        }

        int assignedCount = calculateAssignments(onlinePlayers).size();

        return Math.max(0, onlineCount - assignedCount);
    }
}
