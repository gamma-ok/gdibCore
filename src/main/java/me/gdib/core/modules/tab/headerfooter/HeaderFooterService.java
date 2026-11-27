package me.gdib.core.modules.tab.headerfooter;

import java.util.List;
import java.util.Objects;

import org.bukkit.entity.Player;

import me.gdib.core.modules.tab.packet.TabPacketAdapter;

public final class HeaderFooterService {

    private final HeaderFooterManager headerFooterManager;
    private final HeaderFooterFormatter headerFooterFormatter;
    private final TabPacketAdapter packetAdapter;

    public HeaderFooterService(
            HeaderFooterManager headerFooterManager,
            HeaderFooterFormatter headerFooterFormatter,
            TabPacketAdapter packetAdapter
    ) {
        this.headerFooterManager = Objects.requireNonNull(
                headerFooterManager,
                "HeaderFooterManager cannot be null"
        );

        this.headerFooterFormatter = Objects.requireNonNull(
                headerFooterFormatter,
                "HeaderFooterFormatter cannot be null"
        );

        this.packetAdapter = Objects.requireNonNull(
                packetAdapter,
                "TabPacketAdapter cannot be null"
        );
    }

    public void update(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");

        HeaderFooterDesign design = headerFooterManager.getDesign(player);

        if (design == null) {
            return;
        }

        List<String> headerLines = headerFooterFormatter.formatLines(
                player,
                design.getHeader()
        );

        List<String> footerLines = headerFooterFormatter.formatLines(
                player,
                design.getFooter()
        );

        String header = String.join("\n", headerLines);
        String footer = String.join("\n", footerLines);

        packetAdapter.sendHeaderFooter(player, header, footer);
    }
}
