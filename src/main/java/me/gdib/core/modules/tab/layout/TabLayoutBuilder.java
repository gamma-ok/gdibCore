package me.gdib.core.modules.tab.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.gdib.core.modules.tab.layout.model.TabLayoutEntry;
import me.gdib.core.modules.tab.layout.model.TabLayoutSlot;

public final class TabLayoutBuilder {

    private final TabLayoutManager layoutManager;
    private final TabPlayerGroupAutoAssignmentManager groupAssignmentManager;

    public TabLayoutBuilder(
            TabLayoutManager layoutManager,
            TabPlayerGroupAutoAssignmentManager groupAssignmentManager
    ) {
        this.layoutManager = Objects.requireNonNull(
                layoutManager,
                "TabLayoutManager cannot be null"
        );

        this.groupAssignmentManager = Objects.requireNonNull(
                groupAssignmentManager,
                "TabPlayerGroupAutoAssignmentManager cannot be null"
        );
    }

    public List<TabLayoutEntry> build(
            Collection<? extends Player> onlinePlayers
    ) {
        Objects.requireNonNull(
                onlinePlayers,
                "Online players cannot be null"
        );

        if (!layoutManager.isEnabled()) {
            return Collections.emptyList();
        }

        // Keep entries ordered by their TAB position.
        Map<Integer, TabLayoutEntry> entries = new TreeMap<>();

        // 1. Add configured custom slots.
        for (TabLayoutSlot slot : layoutManager.getSlots().values()) {

            if (slot.hasTexture()) {
                entries.put(
                        slot.getPosition(),
                        TabLayoutEntry.customHead(
                                slot.getPosition(),
                                slot.hasText() ? slot.getText() : null,
                                slot.getTexture()
                        )
                );

            } else if (slot.hasText()) {
                entries.put(
                        slot.getPosition(),
                        TabLayoutEntry.text(
                                slot.getPosition(),
                                slot.getText()
                        )
                );
            }
        }

        // 2. Calculate manual and automatic player assignments.
        Map<Integer, UUID> playerAssignments =
                groupAssignmentManager.calculateAssignments(onlinePlayers);

        // 3. Add players without overriding configured custom slots.
        for (Map.Entry<Integer, UUID> assignment :
                playerAssignments.entrySet()) {

            int position = assignment.getKey();

            if (entries.containsKey(position)) {
                continue;
            }

            entries.put(
                    position,
                    TabLayoutEntry.player(
                            position,
                            assignment.getValue()
                    )
            );
        }

        // 4. Return an immutable, position-ordered result.
        return Collections.unmodifiableList(
                new ArrayList<>(entries.values())
        );
    }
}
