package me.gdib.core.modules.tab.packet.v1_8_R3;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.Packet;

public final class V1_8_R3PacketSender {

	/**
	 * Sends an NMS packet to a connected player.
	 *
	 * @param player packet recipient
	 * @param packet packet to send
	 */
	public void send(Player player, Packet<?> packet) {
		Objects.requireNonNull(player, "Player cannot be null");

		Objects.requireNonNull(packet, "Packet cannot be null");

		if (!Bukkit.isPrimaryThread()) {
			throw new IllegalStateException("NMS packets must be sent from the main thread.");
		}

		if (!player.isOnline()) {
			return;
		}

		CraftPlayer craftPlayer = (CraftPlayer) player;

		craftPlayer.getHandle().playerConnection.sendPacket(packet);
	}
}
