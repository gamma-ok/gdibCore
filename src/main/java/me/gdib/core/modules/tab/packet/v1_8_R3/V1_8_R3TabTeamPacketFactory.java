package me.gdib.core.modules.tab.packet.v1_8_R3;

import java.util.Objects;
import java.util.UUID;

import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R3.Scoreboard;
import net.minecraft.server.v1_8_R3.ScoreboardTeam;

public final class V1_8_R3TabTeamPacketFactory {

	private final V1_8_R3TabSortKeyFactory sortKeyFactory;

	public V1_8_R3TabTeamPacketFactory(V1_8_R3TabSortKeyFactory sortKeyFactory) {
		this.sortKeyFactory = Objects.requireNonNull(sortKeyFactory, "SortKeyFactory cannot be null");
	}

	/**
	 * Creates a team packet containing one TAB entry. Mode 0: create team.
	 */
	public PacketPlayOutScoreboardTeam createTeamPacket(UUID viewerId, int position, String profileName) {
		validateProfileName(profileName);

		ScoreboardTeam team = createTeam(viewerId, position);

		team.getPlayerNameSet().add(profileName);

		return new PacketPlayOutScoreboardTeam(team, 0);
	}

	/**
	 * Mode 3: add a profile to an existing team.
	 */
	public PacketPlayOutScoreboardTeam createAddEntryPacket(UUID viewerId, int position, String profileName) {
		validateProfileName(profileName);

		ScoreboardTeam team = createTeam(viewerId, position);

		team.getPlayerNameSet().add(profileName);

		return new PacketPlayOutScoreboardTeam(team, 3);
	}

	/**
	 * Mode 4: remove a profile from an existing team.
	 */
	public PacketPlayOutScoreboardTeam createRemoveEntryPacket(UUID viewerId, int position, String profileName) {
		validateProfileName(profileName);

		ScoreboardTeam team = createTeam(viewerId, position);

		team.getPlayerNameSet().add(profileName);

		return new PacketPlayOutScoreboardTeam(team, 4);
	}

	/**
	 * Mode 1: remove an entire team.
	 */
	public PacketPlayOutScoreboardTeam createRemoveTeamPacket(UUID viewerId, int position) {
		return new PacketPlayOutScoreboardTeam(createTeam(viewerId, position), 1);
	}

	private ScoreboardTeam createTeam(UUID viewerId, int position) {
		Objects.requireNonNull(viewerId, "Viewer UUID cannot be null");

		String teamName = sortKeyFactory.createTeamName(viewerId, position);

		Scoreboard scoreboard = new Scoreboard();

		ScoreboardTeam team = new ScoreboardTeam(scoreboard, teamName);

		team.setDisplayName(teamName);
		team.setPrefix("");
		team.setSuffix("");

		return team;
	}

	private void validateProfileName(String profileName) {
		Objects.requireNonNull(profileName, "Profile name cannot be null");

		if (profileName.isBlank() || profileName.length() > 16) {
			throw new IllegalArgumentException("Profile name must contain between 1 and 16 characters.");
		}
	}
}
