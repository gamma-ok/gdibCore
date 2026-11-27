package me.gdib.core.modules.tab.packet.v1_8_R3;

import java.util.List;
import java.util.Objects;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;

public final class V1_8_R3TabPacketFactory {

	/**
	 * Creates a packet that adds fake players to the TAB list.
	 *
	 * @param players fake players to register
	 * @return ADD_PLAYER packet
	 */
	public PacketPlayOutPlayerInfo createAddPacket(List<EntityPlayer> players) {
		validatePlayers(players);

		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, players.toArray(new EntityPlayer[0]));
	}

	/**
	 * Creates a packet that removes fake players from the TAB list.
	 *
	 * @param players fake players to remove
	 * @return REMOVE_PLAYER packet
	 */
	public PacketPlayOutPlayerInfo createRemovePacket(List<EntityPlayer> players) {
		validatePlayers(players);

		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, players.toArray(new EntityPlayer[0]));
	}

	private void validatePlayers(List<EntityPlayer> players) {

		Objects.requireNonNull(players, "Player list cannot be null");

		if (players.isEmpty()) {
			throw new IllegalArgumentException("Player list cannot be empty.");
		}

		for (EntityPlayer player : players) {
			Objects.requireNonNull(player, "Player list cannot contain null elements.");
		}
	}
}
