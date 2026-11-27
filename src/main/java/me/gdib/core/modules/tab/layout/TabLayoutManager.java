package me.gdib.core.modules.tab.layout;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import me.gdib.core.modules.tab.layout.config.TabLayoutConfig;
import me.gdib.core.modules.tab.layout.model.TabLayoutGroup;
import me.gdib.core.modules.tab.layout.model.TabLayoutSlot;

public final class TabLayoutManager {

    private final TabLayoutConfig config;

    private Map<Integer, TabLayoutSlot> slots;
    private Map<String, TabLayoutGroup> groups;

    public TabLayoutManager(TabLayoutConfig config) {
        this.config = Objects.requireNonNull(
                config,
                "TabLayoutConfig cannot be null"
        );

        refresh();
    }

    public void refresh() {
        Map<Integer, TabLayoutSlot> newSlots = new LinkedHashMap<>();

        for (TabLayoutSlot slot : config.getSlots()) {
            if (newSlots.putIfAbsent(
                    slot.getPosition(),
                    slot
            ) != null) {
                throw new IllegalArgumentException(
                        "Duplicate TAB layout position: "
                        + slot.getPosition()
                );
            }
        }

        Map<String, TabLayoutGroup> newGroups = new LinkedHashMap<>();

        for (TabLayoutGroup group : config.getGroups()) {
            String key = group.getName().toLowerCase(
                    java.util.Locale.ROOT
            );

            if (newGroups.putIfAbsent(key, group) != null) {
                throw new IllegalArgumentException(
                        "Duplicate TAB layout group: "
                        + group.getName()
                );
            }
        }

        slots = Collections.unmodifiableMap(newSlots);
        groups = Collections.unmodifiableMap(newGroups);
    }

    public boolean isEnabled() {
        return config.isEnabled();
    }

    public TabLayoutConfig getConfig() {
        return config;
    }

    public TabLayoutSlot getSlot(int position) {
        return slots.get(position);
    }

    public TabLayoutGroup getGroup(String name) {
        if (name == null) {
            return null;
        }

        return groups.get(
                name.toLowerCase(java.util.Locale.ROOT)
        );
    }

    public TabLayoutGroup getGroupForSlot(int position) {
        TabLayoutGroup selectedGroup = null;

        for (TabLayoutGroup group : groups.values()) {
            if (!group.containsSlot(position)) {
                continue;
            }

            if (selectedGroup == null
                    || group.getPriority() > selectedGroup.getPriority()) {
                selectedGroup = group;
            }
        }

        return selectedGroup;
    }

    public Map<Integer, TabLayoutSlot> getSlots() {
        return slots;
    }

    public Map<String, TabLayoutGroup> getGroups() {
        return groups;
    }

    public int getSlotCount() {
        return slots.size();
    }

    public int getGroupCount() {
        return groups.size();
    }

    public boolean hasSlot(int position) {
        return slots.containsKey(position);
    }

    public boolean hasGroup(String name) {
        return getGroup(name) != null;
    }
}
