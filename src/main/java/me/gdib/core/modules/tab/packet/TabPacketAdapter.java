package me.gdib.core.modules.tab.packet;

import org.bukkit.entity.Player;

public interface TabPacketAdapter {

    void sendHeaderFooter(
            Player player,
            String header,
            String footer
    );
}
