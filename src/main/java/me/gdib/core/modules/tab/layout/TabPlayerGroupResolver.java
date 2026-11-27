package me.gdib.core.modules.tab.layout;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.gdib.core.modules.tab.layout.model.TabLayoutGroup;
import me.gdib.core.modules.tab.layout.model.TabPlayerGroupAssignment;

public final class TabPlayerGroupResolver {

    private static final String DEFAULT_GROUP = "_DEFAULT_";

    private final TabLayoutManager layoutManager;
    private final TabPlayerGroupAssignmentManager assignmentManager;

    public TabPlayerGroupResolver(
            TabLayoutManager layoutManager,
            TabPlayerGroupAssignmentManager assignmentManager
    ) {
        this.layoutManager = Objects.requireNonNull(
                layoutManager,
                "TabLayoutManager cannot be null"
        );

        this.assignmentManager = Objects.requireNonNull(
                assignmentManager,
                "TabPlayerGroupAssignmentManager cannot be null"
        );
    }

    public TabLayoutGroup resolve(Player player) {

        Objects.requireNonNull(
                player,
                "Player cannot be null"
        );

        UUID playerId = player.getUniqueId();

        // 1. Manual group assignment has the highest priority.
        TabPlayerGroupAssignment manualAssignment =
                assignmentManager.getAssignment(playerId);

        if (manualAssignment != null) {

            TabLayoutGroup assignedGroup = layoutManager.getGroup(
                    manualAssignment.getGroupName()
            );

            if (assignedGroup != null) {
                return assignedGroup;
            }
        }

        // 2. Find the highest-priority matching group.
        TabLayoutGroup selectedGroup = null;

        for (Map.Entry<String, TabLayoutGroup> entry :
                layoutManager.getGroups().entrySet()) {

            TabLayoutGroup group = entry.getValue();

            // The default group is only a fallback.
            if (group.getName().equalsIgnoreCase(DEFAULT_GROUP)) {
                continue;
            }

            String permission = group.getPermission();

            // Groups without a permission match every player.
            boolean matches = permission == null
                    || permission.isBlank()
                    || player.hasPermission(permission);

            if (!matches) {
                continue;
            }

            if (selectedGroup == null
                    || group.getPriority() > selectedGroup.getPriority()) {

                selectedGroup = group;
            }
        }

        if (selectedGroup != null) {
            return selectedGroup;
        }

        // 3. Use the default group when no other group matches.
        return layoutManager.getGroup(DEFAULT_GROUP);
    }

    public boolean hasResolvedGroup(Player player) {
        return resolve(player) != null;
    }
}
