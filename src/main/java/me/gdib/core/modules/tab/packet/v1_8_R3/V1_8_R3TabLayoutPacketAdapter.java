package me.gdib.core.modules.tab.packet.v1_8_R3;

import java.util.List;
import java.util.Objects;

import org.bukkit.entity.Player;

import me.gdib.core.modules.tab.layout.model.TabLayoutEntry;
import me.gdib.core.modules.tab.packet.TabLayoutPacketAdapter;

public final class V1_8_R3TabLayoutPacketAdapter implements TabLayoutPacketAdapter {

    private final V1_8_R3TabLayoutController controller;

    public V1_8_R3TabLayoutPacketAdapter(
            V1_8_R3TabLayoutController controller) {

        this.controller = Objects.requireNonNull(
                controller,
                "controller cannot be null"
        );
    }

    @Override
    public void updateLayout(Player player, List<TabLayoutEntry> entries) {
        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(entries, "entries cannot be null");

        controller.update(player, entries);
    }

    @Override
    public void clearLayout(Player player) {
        Objects.requireNonNull(player, "player cannot be null");

        controller.clear(player);
    }
}
