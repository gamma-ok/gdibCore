package me.gdib.core.modules.tab.layout.model;

import java.util.Objects;

public final class TabLayoutGroup {

    private final String name;
    private final int startSlot;
    private final int endSlot;
    private final int priority;
    private final String permission;

    public TabLayoutGroup(
            String name,
            int startSlot,
            int endSlot,
            int priority,
            String permission
    ) {
        this.name = Objects.requireNonNull(
                name,
                "Group name cannot be null"
        ).trim();

        if (this.name.isEmpty()) {
            throw new IllegalArgumentException(
                    "Group name cannot be empty."
            );
        }

        if (startSlot < 1 || endSlot > 80 || startSlot > endSlot) {
            throw new IllegalArgumentException(
                    "Invalid TAB group slot range: "
                    + startSlot + "-" + endSlot
            );
        }

        this.startSlot = startSlot;
        this.endSlot = endSlot;
        this.priority = priority;

        this.permission = permission == null
                ? null
                : permission.trim();

        if (this.permission != null && this.permission.isEmpty()) {
            throw new IllegalArgumentException(
                    "Group permission cannot be empty."
            );
        }
    }

    public String getName() {
        return name;
    }

    public int getStartSlot() {
        return startSlot;
    }

    public int getEndSlot() {
        return endSlot;
    }

    public int getPriority() {
        return priority;
    }

    public String getPermission() {
        return permission;
    }

    public boolean hasPermission() {
        return permission != null;
    }

    public boolean containsSlot(int slot) {
        return slot >= startSlot && slot <= endSlot;
    }

    public int getCapacity() {
        return endSlot - startSlot + 1;
    }
}
