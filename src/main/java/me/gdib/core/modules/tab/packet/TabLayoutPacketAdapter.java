package me.gdib.core.modules.tab.packet;

import java.util.List;

import org.bukkit.entity.Player;

import me.gdib.core.modules.tab.layout.model.TabLayoutEntry;

public interface TabLayoutPacketAdapter {

    /**
     * Updates the player's custom TAB layout.
     *
     * @param player target player
     * @param entries ordered layout entries
     */
    void updateLayout(
            Player player,
            List<TabLayoutEntry> entries
    );

    /**
     * Removes the custom TAB layout from the player
     * and restores the normal player list.
     *
     * @param player target player
     */
    void clearLayout(Player player);
}
