package me.gdib.core.modules.tab.placeholder;

import java.util.Objects;

import me.gdib.core.common.placeholder.PlaceholderManager;
import me.gdib.core.common.player.PlayerManager;

public final class TabPlaceholderRegistrar {

    private final PlaceholderManager placeholderManager;
    private final PlayerManager playerManager;

    private boolean registered;

    public TabPlaceholderRegistrar(
            PlaceholderManager placeholderManager,
            PlayerManager playerManager
    ) {
        this.placeholderManager = Objects.requireNonNull(
                placeholderManager,
                "PlaceholderManager cannot be null"
        );

        this.playerManager = Objects.requireNonNull(
                playerManager,
                "PlayerManager cannot be null"
        );
    }

    public void register() {
        if (registered) {
            return;
        }

        placeholderManager.register(
                "online_players",
                player -> String.valueOf(
                        playerManager.getOnlineCount()
                )
        );

        registered = true;
    }

    public void unregister() {
        if (!registered) {
            return;
        }

        placeholderManager.unregister("online_players");

        registered = false;
    }

    public boolean isRegistered() {
        return registered;
    }
}
