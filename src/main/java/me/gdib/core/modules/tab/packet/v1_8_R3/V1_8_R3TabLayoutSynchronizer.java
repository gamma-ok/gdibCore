package me.gdib.core.modules.tab.packet.v1_8_R3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.gdib.core.modules.tab.layout.model.TabLayoutEntry;

import net.minecraft.server.v1_8_R3.EntityPlayer;

public final class V1_8_R3TabLayoutSynchronizer {

	private final V1_8_R3TabFakeEntryManager fakeEntryManager;
	private final V1_8_R3TabPacketFactory packetFactory;
	private final V1_8_R3PacketSender packetSender;

	/*
	 * Viewer UUID -> Position -> Entry signature
	 */
	private final Map<UUID, Map<Integer, String>> synchronizedEntries = new LinkedHashMap<>();

	public V1_8_R3TabLayoutSynchronizer(V1_8_R3TabFakeEntryManager fakeEntryManager,
			V1_8_R3TabPacketFactory packetFactory, V1_8_R3PacketSender packetSender) {
		this.fakeEntryManager = Objects.requireNonNull(fakeEntryManager, "FakeEntryManager cannot be null");

		this.packetFactory = Objects.requireNonNull(packetFactory, "PacketFactory cannot be null");

		this.packetSender = Objects.requireNonNull(packetSender, "PacketSender cannot be null");
	}

	/**
	 * Synchronizes fake entries with the desired layout.
	 *
	 * @param viewer        recipient player
	 * @param layoutEntries desired layout entries
	 */
	public void synchronize(Player viewer, List<TabLayoutEntry> layoutEntries) {
		ensureMainThread();

		Objects.requireNonNull(viewer, "Viewer cannot be null");

		Objects.requireNonNull(layoutEntries, "Layout entries cannot be null");

		if (!viewer.isOnline()) {
			return;
		}

		UUID viewerId = viewer.getUniqueId();

		Map<Integer, TabLayoutEntry> desiredEntries = new TreeMap<>();

		for (TabLayoutEntry entry : layoutEntries) {

			Objects.requireNonNull(entry, "Layout cannot contain null entries");

			if (entry.isPlayer()) {
				continue;
			}

			if (desiredEntries.put(entry.getPosition(), entry) != null) {
				throw new IllegalArgumentException("Duplicate layout position: " + entry.getPosition());
			}
		}

		Map<Integer, String> previousEntries = synchronizedEntries.getOrDefault(viewerId, Collections.emptyMap());

		Map<Integer, String> updatedEntries = new LinkedHashMap<>();

		List<EntityPlayer> removedPlayers = new ArrayList<>();

		// 1. Detect entries that are no longer valid.
		for (Map.Entry<Integer, String> previous : previousEntries.entrySet()) {

			int position = previous.getKey();

			TabLayoutEntry desired = desiredEntries.get(position);

			if (desired == null || !previous.getValue().equals(createSignature(desired))) {

				EntityPlayer removed = fakeEntryManager.remove(viewerId, position);

				if (removed != null) {
					removedPlayers.add(removed);
				}
			}
		}

		// 2. Remove outdated entries from the client.
		if (!removedPlayers.isEmpty()) {
			packetSender.send(viewer, packetFactory.createRemovePacket(removedPlayers));
		}

		// 3. Preserve entries that have not changed.
		for (Map.Entry<Integer, String> previous : previousEntries.entrySet()) {

			int position = previous.getKey();

			TabLayoutEntry desired = desiredEntries.get(position);

			if (desired != null && previous.getValue().equals(createSignature(desired))) {

				updatedEntries.put(position, previous.getValue());
			}
		}

		// 4. Create new or modified entries.
		List<EntityPlayer> addedPlayers = new ArrayList<>();

		for (TabLayoutEntry desired : desiredEntries.values()) {

			int position = desired.getPosition();

			if (updatedEntries.containsKey(position)) {
				continue;
			}

			EntityPlayer fakePlayer = fakeEntryManager.create(viewer, desired);

			addedPlayers.add(fakePlayer);

			updatedEntries.put(position, createSignature(desired));
		}

		// 5. Register new entries on the client.
		if (!addedPlayers.isEmpty()) {
			packetSender.send(viewer, packetFactory.createAddPacket(addedPlayers));
		}

		// 6. Save the new synchronization state.
		if (updatedEntries.isEmpty()) {
			synchronizedEntries.remove(viewerId);
		} else {
			synchronizedEntries.put(viewerId, updatedEntries);
		}
	}

	/**
	 * Removes all fake entries belonging to a viewer.
	 */
	public void clear(Player viewer) {
		ensureMainThread();

		Objects.requireNonNull(viewer, "Viewer cannot be null");

		clear(viewer.getUniqueId(), viewer);
	}

	/**
	 * Removes all fake entries belonging to a viewer UUID.
	 *
	 * The packet is sent only when the viewer is online.
	 */
	public void clear(UUID viewerId, Player viewer) {
		ensureMainThread();

		Objects.requireNonNull(viewerId, "Viewer UUID cannot be null");

		Objects.requireNonNull(viewer, "Viewer cannot be null");

		if (!viewerId.equals(viewer.getUniqueId())) {
			throw new IllegalArgumentException("Viewer UUID does not match the player.");
		}

		List<EntityPlayer> removedPlayers = fakeEntryManager.clear(viewerId);

		synchronizedEntries.remove(viewerId);

		if (!removedPlayers.isEmpty() && viewer.isOnline()) {
			packetSender.send(viewer, packetFactory.createRemovePacket(removedPlayers));
		}
	}

	/**
	 * Removes synchronization state for a disconnected viewer.
	 *
	 * No packet is sent because the player is offline.
	 */
	public void forget(UUID viewerId) {
		ensureMainThread();

		Objects.requireNonNull(viewerId, "Viewer UUID cannot be null");

		fakeEntryManager.clear(viewerId);
		synchronizedEntries.remove(viewerId);
	}

	public boolean isSynchronized(UUID viewerId) {
		ensureMainThread();

		Objects.requireNonNull(viewerId, "Viewer UUID cannot be null");

		return synchronizedEntries.containsKey(viewerId);
	}

	public int getSynchronizedEntryCount(UUID viewerId) {
		ensureMainThread();

		Objects.requireNonNull(viewerId, "Viewer UUID cannot be null");

		Map<Integer, String> viewerEntries = synchronizedEntries.get(viewerId);

		return viewerEntries == null ? 0 : viewerEntries.size();
	}

	/**
	 * Generates a signature to detect changes in an entry.
	 */
	private String createSignature(TabLayoutEntry entry) {

		StringBuilder signature = new StringBuilder();

		signature.append(entry.getType().name());

		if (entry.isText() || entry.isCustomHead()) {
			appendValue(signature, entry.getText());
		}

		if (entry.isCustomHead()) {
			appendValue(signature, entry.getTexture().getValue());

			appendValue(signature, entry.getTexture().getSignature());
		}

		return signature.toString();
	}

	private void appendValue(StringBuilder builder, String value) {
		if (value == null) {
			builder.append("-1:");
			return;
		}

		builder.append(value.length()).append(':').append(value);
	}

	private void ensureMainThread() {
		if (!Bukkit.isPrimaryThread()) {
			throw new IllegalStateException("TAB layout synchronization must run " + "on the main server thread.");
		}
	}
}
