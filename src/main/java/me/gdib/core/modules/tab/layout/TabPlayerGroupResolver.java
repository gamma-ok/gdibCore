package me.gdib.core.modules.tab.layout;

import java.util.Objects;

import org.bukkit.entity.Player;

import me.gdib.core.modules.tab.layout.model.TabLayoutGroup;

public final class TabPlayerGroupResolver {

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

        // 1. Check for a manual group assignment.
        String assignedGroup = assignmentManager.getAssignedGroup(
                player.getUniqueId()
        );

        if (assignedGroup != null) {
            TabLayoutGroup manualGroup =
                    layoutManager.getGroup(assignedGroup);

            if (manualGroup != null) {
                return manualGroup;
            }
        }

        // 2. Resolve the group using player permissions.
        TabLayoutGroup selectedGroup = null;

        for (TabLayoutGroup group :
                layoutManager.getGroups().values()) {

            if (!matches(player, group)) {
                continue;
            }

            if (selectedGroup == null
                    || group.getPriority() > selectedGroup.getPriority()) {
                selectedGroup = group;
            }
        }

        return selectedGroup;
    }

    private boolean matches(
            Player player,
            TabLayoutGroup group
    ) {
        if (!group.hasPermission()) {
            return true;
        }

        return player.hasPermission(group.getPermission());
    }

    public boolean belongsToGroup(
            Player player,
            String groupName
    ) {
        if (player == null || groupName == null) {
            return false;
        }

        TabLayoutGroup group = resolve(player);

        return group != null
                && group.getName().equalsIgnoreCase(groupName);
    }

    public boolean hasGroup(Player player) {
        return player != null && resolve(player) != null;
    }

    public boolean hasManualAssignment(Player player) {
        return player != null
                && assignmentManager.hasAssignment(
                        player.getUniqueId()
                );
    }
}
