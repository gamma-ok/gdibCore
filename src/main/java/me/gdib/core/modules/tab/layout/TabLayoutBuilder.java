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

import me.gdib.core.modules.tab.layout.model.TabLayoutDirection;
import me.gdib.core.modules.tab.layout.model.TabLayoutEntry;
import me.gdib.core.modules.tab.layout.model.TabLayoutSlot;
import me.gdib.core.modules.tab.layout.model.TabSkinTexture;
import me.gdib.core.modules.tab.skin.TabSkinTextureService;

public final class TabLayoutBuilder {

    private static final int MAX_SLOTS = 80;
    private static final int ROWS = 20;
    private static final int COLUMNS = 4;

    private final TabLayoutManager layoutManager;
    private final TabPlayerGroupAutoAssignmentManager groupAssignmentManager;
    private final TabSkinTextureService textureService;

    public TabLayoutBuilder(
            TabLayoutManager layoutManager,
            TabPlayerGroupAutoAssignmentManager groupAssignmentManager,
            TabSkinTextureService textureService
    ) {
        this.layoutManager = Objects.requireNonNull(
                layoutManager,
                "TabLayoutManager cannot be null"
        );

        this.groupAssignmentManager = Objects.requireNonNull(
                groupAssignmentManager,
                "TabPlayerGroupAutoAssignmentManager cannot be null"
        );

        this.textureService = Objects.requireNonNull(
                textureService,
                "TabSkinTextureService cannot be null"
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

        // Store entries using their final physical TAB positions.
        Map<Integer, TabLayoutEntry> entries = new TreeMap<>();

        // 1. Add configured custom slots.
        for (TabLayoutSlot slot : layoutManager.getSlots().values()) {

            int position = toPhysicalPosition(slot.getPosition());

            if (slot.hasTexture()) {

                // Try the configured texture first.
                TabSkinTexture texture = textureService.getTexture(
                        slot.getTexture()
                );

                // Use the default skin as a fallback.
                if (texture == null) {

                    String defaultSkin =
                            layoutManager.getConfig().getDefaultSkin();

                    if (defaultSkin != null && !defaultSkin.isBlank()) {
                        texture = textureService.getTexture(defaultSkin);
                    }
                }

                if (texture != null) {

                    entries.put(
                            position,
                            TabLayoutEntry.customHead(
                                    position,
                                    slot.hasText() ? slot.getText() : null,
                                    texture
                            )
                    );

                } else if (slot.hasText()) {

                    entries.put(
                            position,
                            TabLayoutEntry.text(position, slot.getText())
                    );
                }

            } else if (slot.hasText()) {

                entries.put(
                        position,
                        TabLayoutEntry.text(position, slot.getText())
                );
            }
        }

        // 2. Calculate manual and automatic player assignments.
        Map<Integer, UUID> playerAssignments =
                groupAssignmentManager.calculateAssignments(onlinePlayers);

        // 3. Add assigned players without overriding custom slots.
        for (Map.Entry<Integer, UUID> assignment :
                playerAssignments.entrySet()) {

            int position = toPhysicalPosition(assignment.getKey());

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

        // 4. Display the remaining players message when possible.
        int remainingPlayers =
                groupAssignmentManager.getUnassignedPlayerCount(onlinePlayers);

        if (remainingPlayers > 0) {

            int availablePosition = findAvailablePosition(entries);

            if (availablePosition != -1) {

                String remainingText = layoutManager.getConfig()
                        .getRemainingPlayersText()
                        .replace("%remaining%", String.valueOf(remainingPlayers));

                if (!remainingText.isBlank()) {

                    entries.put(
                            availablePosition,
                            TabLayoutEntry.text(
                                    availablePosition,
                                    remainingText
                            )
                    );
                }
            }
        }

        // 5. Return an immutable, position-ordered result.
        return Collections.unmodifiableList(
                new ArrayList<>(entries.values())
        );
    }

    private int toPhysicalPosition(int logicalPosition) {

        if (logicalPosition < 1 || logicalPosition > MAX_SLOTS) {
            throw new IllegalArgumentException(
                    "TAB position must be between 1 and 80."
            );
        }

        TabLayoutDirection direction =
                layoutManager.getConfig().getDirection();

        if (direction == TabLayoutDirection.COLUMNS) {
            return logicalPosition;
        }

        // Convert row-major positions into column-major positions.
        int zeroBasedPosition = logicalPosition - 1;

        int row = zeroBasedPosition / COLUMNS;
        int column = zeroBasedPosition % COLUMNS;

        return (column * ROWS) + row + 1;
    }

    private int findAvailablePosition(
            Map<Integer, TabLayoutEntry> entries
    ) {

        for (int position = 1; position <= MAX_SLOTS; position++) {

            if (!entries.containsKey(position)) {
                return position;
            }
        }

        return -1;
    }
}
