package me.gdib.core.common.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PlayerManager {

    // =========================================================
    // PLAYER LOOKUP
    // =========================================================

    public Optional<Player> getPlayer(UUID playerId) {
        ensureMainThread();

        if (playerId == null) {
            throw new IllegalArgumentException(
                    "Player UUID cannot be null"
            );
        }

        return Optional.ofNullable(
                Bukkit.getPlayer(playerId)
        );
    }

    public Optional<Player> getPlayer(String playerName) {
        ensureMainThread();

        if (playerName == null || playerName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Player name cannot be null or empty"
            );
        }

        return Optional.ofNullable(
                Bukkit.getPlayerExact(playerName)
        );
    }

    // =========================================================
    // PLAYER STATUS
    // =========================================================

    public boolean isOnline(UUID playerId) {
        return getPlayer(playerId).isPresent();
    }

    public boolean isOnline(String playerName) {
        return getPlayer(playerName).isPresent();
    }

    // =========================================================
    // ONLINE PLAYERS
    // =========================================================

    public Collection<Player> getOnlinePlayers() {
        ensureMainThread();

        List<Player> onlinePlayers = new ArrayList<>(
                Bukkit.getOnlinePlayers()
        );

        return Collections.unmodifiableList(onlinePlayers);
    }

    public int getOnlineCount() {
        ensureMainThread();

        return Bukkit.getOnlinePlayers().size();
    }

    public List<String> getOnlinePlayerNames() {
        ensureMainThread();

        List<String> names = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }

        return Collections.unmodifiableList(names);
    }

    // =========================================================
    // THREAD VALIDATION
    // =========================================================

    private void ensureMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException(
                    "PlayerManager operations must be executed "
                            + "on the main server thread."
            );
        }
    }
}
