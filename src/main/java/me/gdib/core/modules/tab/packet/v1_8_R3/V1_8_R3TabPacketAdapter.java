package me.gdib.core.modules.tab.packet.v1_8_R3;

import java.lang.reflect.Field;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;

import me.gdib.core.modules.tab.packet.TabPacketAdapter;

public final class V1_8_R3TabPacketAdapter implements TabPacketAdapter {

    private static final String HEADER_FIELD = "a";
    private static final String FOOTER_FIELD = "b";

    @Override
    public void sendHeaderFooter(
            Player player,
            String header,
            String footer
    ) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(header, "Header cannot be null");
        Objects.requireNonNull(footer, "Footer cannot be null");

        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException(
                    "Tab packets must be sent from the main thread"
            );
        }

        if (!player.isOnline()) {
            return;
        }

        PacketPlayOutPlayerListHeaderFooter packet =
                new PacketPlayOutPlayerListHeaderFooter();

        try {
            setPacketComponent(packet, HEADER_FIELD, header);
            setPacketComponent(packet, FOOTER_FIELD, footer);

            CraftPlayer craftPlayer = (CraftPlayer) player;

            craftPlayer.getHandle()
                    .playerConnection
                    .sendPacket(packet);

        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Could not construct the TAB header/footer packet",
                    exception
            );
        }
    }

    private void setPacketComponent(
            PacketPlayOutPlayerListHeaderFooter packet,
            String fieldName,
            String text
    ) throws ReflectiveOperationException {

        Field field = packet.getClass().getDeclaredField(fieldName);

        field.setAccessible(true);
        field.set(packet, new ChatComponentText(text));
    }
}
