package me.gdib.core.modules.tab.packet.v1_8_R3;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.gdib.core.modules.tab.layout.model.TabLayoutEntry;

import net.minecraft.server.v1_8_R3.EntityPlayer;

public final class V1_8_R3TabLayoutTeamSynchronizer {

    private final V1_8_R3TabTeamManager teamManager;

    private final V1_8_R3TabFakeEntryManager fakeEntryManager;

    public V1_8_R3TabLayoutTeamSynchronizer(
            V1_8_R3TabTeamManager teamManager,
            V1_8_R3TabFakeEntryManager fakeEntryManager
    ) {
        this.teamManager = Objects.requireNonNull(
                teamManager,
                "TeamManager cannot be null"
        );

        this.fakeEntryManager = Objects.requireNonNull(
                fakeEntryManager,
                "FakeEntryManager cannot be null"
        );
    }

    /**
     * Synchronizes team membership for real and fake TAB entries.
     *
     * Must be called after fake entries have been synchronized.
     */
    public void synchronize(
            Player viewer,
            Iterable<TabLayoutEntry> layoutEntries
    ) {
        ensureMainThread();

        Objects.requireNonNull(
                viewer,
                "Viewer cannot be null"
        );

        Objects.requireNonNull(
                layoutEntries,
                "Layout entries cannot be null"
        );

        if (!viewer.isOnline()) {
            return;
        }

        UUID viewerId = viewer.getUniqueId();

        // 1. Build the desired position-to-profile mapping.
        Map<Integer, String> desiredTeams = new TreeMap<>();

        for (TabLayoutEntry entry : layoutEntries) {

            Objects.requireNonNull(
                    entry,
                    "Layout cannot contain null entries"
            );

            String profileName = resolveProfileName(
                    viewer,
                    entry
            );

            if (profileName == null) {
                continue;
            }

            String previous = desiredTeams.put(
                    entry.getPosition(),
                    profileName
            );

            if (previous != null) {
                throw new IllegalArgumentException(
                        "Duplicate layout position: "
                        + entry.getPosition()
                );
            }
        }

        // 2. Retrieve the current team assignments.
        Map<Integer, String> currentTeams =
                teamManager.getRegisteredTeams(viewerId);

        // 3. Remove obsolete or modified teams.
        for (Map.Entry<Integer, String> current :
                currentTeams.entrySet()) {

            int position = current.getKey();

            String desiredName = desiredTeams.get(position);

            if (desiredName == null
                    || !desiredName.equals(current.getValue())) {

                teamManager.unregister(
                        viewer,
                        position
                );
            }
        }

        // 4. Register missing team assignments.
        Map<Integer, String> updatedTeams =
                teamManager.getRegisteredTeams(viewerId);

        for (Map.Entry<Integer, String> desired :
                desiredTeams.entrySet()) {

            int position = desired.getKey();

            if (updatedTeams.containsKey(position)) {
                continue;
            }

            teamManager.register(
                    viewer,
                    position,
                    desired.getValue()
            );
        }
    }

    /**
     * Resolves the profile name associated with an entry.
     *
     * Real players use their actual Minecraft profile name.
     * Fake entries use their registered EntityPlayer profile name.
     */
    private String resolveProfileName(
            Player viewer,
            TabLayoutEntry entry
    ) {
        if (entry.isPlayer()) {

            Player target = Bukkit.getPlayer(
                    entry.getPlayerId()
            );

            if (target == null || !target.isOnline()) {
                return null;
            }

            return target.getName();
        }

        EntityPlayer fakePlayer = fakeEntryManager.get(
                viewer.getUniqueId(),
                entry.getPosition()
        );

        return fakePlayer == null
                ? null
                : fakePlayer.getName();
    }

    private void ensureMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException(
                    "TAB team synchronization must run "
                    + "on the main server thread."
            );
        }
    }
}
