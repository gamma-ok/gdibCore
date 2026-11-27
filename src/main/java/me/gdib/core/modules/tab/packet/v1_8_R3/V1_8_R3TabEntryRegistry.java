package me.gdib.core.modules.tab.packet.v1_8_R3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;

import net.minecraft.server.v1_8_R3.EntityPlayer;

public final class V1_8_R3TabEntryRegistry {

	/*
	 * Viewer UUID -> Position -> Fake EntityPlayer
	 */
	private final Map<UUID, Map<Integer, EntityPlayer>> entries = new LinkedHashMap<>();

	/**
	 * Registers a fake player entry for a viewer.
	 *
	 * @param viewerId   recipient player UUID
	 * @param position   TAB position
	 * @param fakePlayer fake player entity
	 * @return previously registered entity, or null
	 */
	public EntityPlayer register(UUID viewerId, int position, EntityPlayer fakePlayer) {
		ensureMainThread();
		validatePosition(position);

		Objects.requireNonNull(viewerId, "Viewer UUID cannot be null");

		Objects.requireNonNull(fakePlayer, "Fake player cannot be null");

		Map<Integer, EntityPlayer> viewerEntries = entries.computeIfAbsent(viewerId, key -> new LinkedHashMap<>());

		return viewerEntries.put(position, fakePlayer);
	}

	/**
	 * Retrieves a registered entry.
	 *
	 * @param viewerId recipient player UUID
	 * @param position TAB position
	 * @return registered entity, or null
	 */
	public EntityPlayer get(UUID viewerId, int position) {
		ensureMainThread();
		validatePosition(position);

		Objects.requireNonNull(viewerId, "Viewer UUID cannot be null");

		Map<Integer, EntityPlayer> viewerEntries = entries.get(viewerId);

		return viewerEntries == null ? null : viewerEntries.get(position);
	}

	/**
	 * Removes an entry from a specific position.
	 *
	 * @param viewerId recipient player UUID
	 * @param position TAB position
	 * @return removed entity, or null
	 */
	public EntityPlayer remove(UUID viewerId, int position) {
		ensureMainThread();
		validatePosition(position);

		Objects.requireNonNull(viewerId, "Viewer UUID cannot be null");

		Map<Integer, EntityPlayer> viewerEntries = entries.get(viewerId);

		if (viewerEntries == null) {
			return null;
		}

		EntityPlayer removed = viewerEntries.remove(position);

		if (viewerEntries.isEmpty()) {
			entries.remove(viewerId);
		}

		return removed;
	}

	/**
	 * Returns an immutable snapshot of all entries belonging to a viewer.
	 */
	public Map<Integer, EntityPlayer> getEntries(UUID viewerId) {
		ensureMainThread();

		Objects.requireNonNull(viewerId, "Viewer UUID cannot be null");

		Map<Integer, EntityPlayer> viewerEntries = entries.get(viewerId);

		if (viewerEntries == null) {
			return Collections.emptyMap();
		}

		return Collections.unmodifiableMap(new LinkedHashMap<>(viewerEntries));
	}

	/**
	 * Returns all fake entities belonging to a viewer.
	 */
	public List<EntityPlayer> getPlayers(UUID viewerId) {
		ensureMainThread();

		Objects.requireNonNull(viewerId, "Viewer UUID cannot be null");

		Map<Integer, EntityPlayer> viewerEntries = entries.get(viewerId);

		if (viewerEntries == null) {
			return Collections.emptyList();
		}

		return Collections.unmodifiableList(new ArrayList<>(viewerEntries.values()));
	}

	/**
	 * Removes all entries belonging to a viewer and returns the removed entities.
	 */
	public List<EntityPlayer> clear(UUID viewerId) {
		ensureMainThread();

		Objects.requireNonNull(viewerId, "Viewer UUID cannot be null");

		Map<Integer, EntityPlayer> removed = entries.remove(viewerId);

		if (removed == null || removed.isEmpty()) {
			return Collections.emptyList();
		}

		return Collections.unmodifiableList(new ArrayList<>(removed.values()));
	}

	/**
	 * Removes all registered viewer entries.
	 */
	public void clearAll() {
		ensureMainThread();
		entries.clear();
	}

	public boolean contains(UUID viewerId, int position) {
		return get(viewerId, position) != null;
	}

	public int getEntryCount(UUID viewerId) {
		ensureMainThread();

		Objects.requireNonNull(viewerId, "Viewer UUID cannot be null");

		Map<Integer, EntityPlayer> viewerEntries = entries.get(viewerId);

		return viewerEntries == null ? 0 : viewerEntries.size();
	}

	public int getViewerCount() {
		ensureMainThread();
		return entries.size();
	}

	private void validatePosition(int position) {
		if (position < 1 || position > 80) {
			throw new IllegalArgumentException("TAB position must be between 1 and 80.");
		}
	}

	private void ensureMainThread() {
		if (!Bukkit.isPrimaryThread()) {
			throw new IllegalStateException("TAB entry registry must be accessed " + "from the main server thread.");
		}
	}
}
