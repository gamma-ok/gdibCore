package me.gdib.core.modules.tab.packet.v1_8_R3;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.gdib.core.modules.tab.layout.model.TabLayoutEntry;

public final class V1_8_R3TabLayoutController {

	private final V1_8_R3TabLayoutSynchronizer layoutSynchronizer;

	private final V1_8_R3TabLayoutTeamSynchronizer teamSynchronizer;

	private final V1_8_R3TabTeamManager teamManager;

	public V1_8_R3TabLayoutController(V1_8_R3TabLayoutSynchronizer layoutSynchronizer,
			V1_8_R3TabLayoutTeamSynchronizer teamSynchronizer, V1_8_R3TabTeamManager teamManager) {
		this.layoutSynchronizer = Objects.requireNonNull(layoutSynchronizer, "LayoutSynchronizer cannot be null");

		this.teamSynchronizer = Objects.requireNonNull(teamSynchronizer, "TeamSynchronizer cannot be null");

		this.teamManager = Objects.requireNonNull(teamManager, "TeamManager cannot be null");
	}

	/**
	 * Updates the complete TAB layout for a viewer.
	 *
	 * Fake entries are synchronized before team membership.
	 */
	public void update(Player viewer, List<TabLayoutEntry> entries) {
		ensureMainThread();

		Objects.requireNonNull(viewer, "Viewer cannot be null");

		Objects.requireNonNull(entries, "Layout entries cannot be null");

		if (!viewer.isOnline()) {
			return;
		}

		// 1. Synchronize fake player profiles.
		layoutSynchronizer.synchronize(viewer, entries);

		// 2. Synchronize real and fake team membership.
		teamSynchronizer.synchronize(viewer, entries);
	}

	/**
	 * Clears the custom layout while the viewer is online.
	 */
	public void clear(Player viewer) {
		ensureMainThread();

		Objects.requireNonNull(viewer, "Viewer cannot be null");

		// Remove fake player profiles.
		layoutSynchronizer.clear(viewer);

		// Remove custom sorting teams.
		teamManager.clear(viewer);
	}

	/**
	 * Releases all local state after a viewer disconnects.
	 *
	 * No packets are sent because the viewer is offline.
	 */
	public void forget(UUID viewerId) {
		ensureMainThread();

		Objects.requireNonNull(viewerId, "Viewer UUID cannot be null");

		layoutSynchronizer.forget(viewerId);
		teamManager.forget(viewerId);
	}

	public boolean isActive(UUID viewerId) {
		ensureMainThread();

		Objects.requireNonNull(viewerId, "Viewer UUID cannot be null");

		return layoutSynchronizer.isSynchronized(viewerId) || teamManager.getTeamCount(viewerId) > 0;
	}

	private void ensureMainThread() {
		if (!Bukkit.isPrimaryThread()) {
			throw new IllegalStateException("TAB layout controller must run " + "on the main server thread.");
		}
	}
}
