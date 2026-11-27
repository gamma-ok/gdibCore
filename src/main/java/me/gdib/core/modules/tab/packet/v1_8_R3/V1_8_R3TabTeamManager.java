package me.gdib.core.modules.tab.packet.v1_8_R3;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;

public final class V1_8_R3TabTeamManager {

    /*
     * Viewer UUID -> Position -> Profile name
     */
    private final Map<UUID, Map<Integer, String>> registeredTeams =
            new LinkedHashMap<>();

    private final V1_8_R3TabTeamPacketFactory packetFactory;

    private final V1_8_R3PacketSender packetSender;

    public V1_8_R3TabTeamManager(
            V1_8_R3TabTeamPacketFactory packetFactory,
            V1_8_R3PacketSender packetSender
    ) {
        this.packetFactory = Objects.requireNonNull(
                packetFactory,
                "TeamPacketFactory cannot be null"
        );

        this.packetSender = Objects.requireNonNull(
                packetSender,
                "PacketSender cannot be null"
        );
    }

    /**
     * Creates and registers a team for a TAB position.
     */
    public void register(
            Player viewer,
            int position,
            String profileName
    ) {
        ensureMainThread();

        Objects.requireNonNull(
                viewer,
                "Viewer cannot be null"
        );

        if (!viewer.isOnline()) {
            return;
        }

        validatePosition(position);
        validateProfileName(profileName);

        UUID viewerId = viewer.getUniqueId();

        Map<Integer, String> viewerTeams =
                registeredTeams.computeIfAbsent(
                        viewerId,
                        key -> new LinkedHashMap<>()
                );

        if (viewerTeams.containsKey(position)) {
            throw new IllegalStateException(
                    "A team is already registered at position "
                    + position + "."
            );
        }

        PacketPlayOutScoreboardTeam packet =
                packetFactory.createTeamPacket(
                        viewerId,
                        position,
                        profileName
                );

        packetSender.send(viewer, packet);

        viewerTeams.put(position, profileName);
    }

    /**
     * Removes a registered team from the client.
     */
    public void unregister(
            Player viewer,
            int position
    ) {
        ensureMainThread();

        Objects.requireNonNull(
                viewer,
                "Viewer cannot be null"
        );

        validatePosition(position);

        UUID viewerId = viewer.getUniqueId();

        Map<Integer, String> viewerTeams =
                registeredTeams.get(viewerId);

        if (viewerTeams == null
                || !viewerTeams.containsKey(position)) {
            return;
        }

        if (viewer.isOnline()) {
            packetSender.send(
                    viewer,
                    packetFactory.createRemoveTeamPacket(
                            viewerId,
                            position
                    )
            );
        }

        viewerTeams.remove(position);

        if (viewerTeams.isEmpty()) {
            registeredTeams.remove(viewerId);
        }
    }

    /**
     * Removes all registered teams belonging to a viewer.
     */
    public void clear(Player viewer) {
        ensureMainThread();

        Objects.requireNonNull(
                viewer,
                "Viewer cannot be null"
        );

        clear(viewer.getUniqueId(), viewer);
    }

    /**
     * Removes all registered teams for a viewer UUID.
     */
    public void clear(UUID viewerId, Player viewer) {
        ensureMainThread();

        Objects.requireNonNull(
                viewerId,
                "Viewer UUID cannot be null"
        );

        Objects.requireNonNull(
                viewer,
                "Viewer cannot be null"
        );

        if (!viewerId.equals(viewer.getUniqueId())) {
            throw new IllegalArgumentException(
                    "Viewer UUID does not match the player."
            );
        }

        Map<Integer, String> viewerTeams =
                registeredTeams.remove(viewerId);

        if (viewerTeams == null || !viewer.isOnline()) {
            return;
        }

        for (Integer position : viewerTeams.keySet()) {
            packetSender.send(
                    viewer,
                    packetFactory.createRemoveTeamPacket(
                            viewerId,
                            position
                    )
            );
        }
    }

    /**
     * Forgets team state after the viewer disconnects.
     */
    public void forget(UUID viewerId) {
        ensureMainThread();

        Objects.requireNonNull(
                viewerId,
                "Viewer UUID cannot be null"
        );

        registeredTeams.remove(viewerId);
    }

    public boolean isRegistered(
            UUID viewerId,
            int position
    ) {
        ensureMainThread();
        validateViewerId(viewerId);
        validatePosition(position);

        Map<Integer, String> viewerTeams =
                registeredTeams.get(viewerId);

        return viewerTeams != null
                && viewerTeams.containsKey(position);
    }

    public String getProfileName(
            UUID viewerId,
            int position
    ) {
        ensureMainThread();
        validateViewerId(viewerId);
        validatePosition(position);

        Map<Integer, String> viewerTeams =
                registeredTeams.get(viewerId);

        return viewerTeams == null
                ? null
                : viewerTeams.get(position);
    }

    public Map<Integer, String> getRegisteredTeams(UUID viewerId) {
        ensureMainThread();
        validateViewerId(viewerId);

        Map<Integer, String> viewerTeams =
                registeredTeams.get(viewerId);

        if (viewerTeams == null) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(
                new LinkedHashMap<>(viewerTeams)
        );
    }

    public int getTeamCount(UUID viewerId) {
        ensureMainThread();
        validateViewerId(viewerId);

        Map<Integer, String> viewerTeams =
                registeredTeams.get(viewerId);

        return viewerTeams == null
                ? 0
                : viewerTeams.size();
    }

    private void validateViewerId(UUID viewerId) {
        Objects.requireNonNull(
                viewerId,
                "Viewer UUID cannot be null"
        );
    }

    private void validatePosition(int position) {
        if (position < 1 || position > 80) {
            throw new IllegalArgumentException(
                    "TAB position must be between 1 and 80."
            );
        }
    }

    private void validateProfileName(String profileName) {
        Objects.requireNonNull(
                profileName,
                "Profile name cannot be null"
        );

        if (profileName.isBlank() || profileName.length() > 16) {
            throw new IllegalArgumentException(
                    "Profile name must contain between 1 and 16 characters."
            );
        }
    }

    private void ensureMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException(
                    "TAB team management must run "
                    + "on the main server thread."
            );
        }
    }
}
