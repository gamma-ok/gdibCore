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

    public Optional<Player> getPlayer(UUID playerId) {
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
        if (playerName == null || playerName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Player name cannot be null or empty"
            );
        }

        return Optional.ofNullable(
            Bukkit.getPlayerExact(playerName)
        );
    }

    public boolean isOnline(UUID playerId) {
        return getPlayer(playerId).isPresent();
    }

    public boolean isOnline(String playerName) {
        return getPlayer(playerName).isPresent();
    }

    public Collection<Player> getOnlinePlayers() {
        List<Player> onlinePlayers = new ArrayList<>(
            Bukkit.getOnlinePlayers()
        );

        return Collections.unmodifiableList(onlinePlayers);
    }

    public int getOnlineCount() {
        return Bukkit.getOnlinePlayers().size();
    }

    public List<String> getOnlinePlayerNames() {
        List<String> names = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }

        return Collections.unmodifiableList(names);
    }
}
