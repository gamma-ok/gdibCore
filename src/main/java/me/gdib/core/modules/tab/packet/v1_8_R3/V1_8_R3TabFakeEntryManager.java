package me.gdib.core.modules.tab.packet.v1_8_R3;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

import me.gdib.core.modules.tab.layout.model.TabLayoutEntry;
import me.gdib.core.modules.tab.layout.model.TabSkinTexture;

import net.minecraft.server.v1_8_R3.EntityPlayer;

public final class V1_8_R3TabFakeEntryManager {

	private final V1_8_R3TabProfileFactory profileFactory;

	private final V1_8_R3FakePlayerFactory fakePlayerFactory;

	private final V1_8_R3TabEntryRegistry entryRegistry;

	private final V1_8_R3TabDisplayNameApplier displayNameApplier;

	public V1_8_R3TabFakeEntryManager(V1_8_R3TabProfileFactory profileFactory,
			V1_8_R3FakePlayerFactory fakePlayerFactory, V1_8_R3TabEntryRegistry entryRegistry,
			V1_8_R3TabDisplayNameApplier displayNameApplier) {
		this.profileFactory = Objects.requireNonNull(profileFactory, "TabProfileFactory cannot be null");

		this.fakePlayerFactory = Objects.requireNonNull(fakePlayerFactory, "FakePlayerFactory cannot be null");

		this.entryRegistry = Objects.requireNonNull(entryRegistry, "TabEntryRegistry cannot be null");

		this.displayNameApplier = Objects.requireNonNull(displayNameApplier, "DisplayNameApplier cannot be null");
	}

	/**
	 * Creates and registers a fake TAB entry.
	 *
	 * Only TEXT and CUSTOM_HEAD entries are supported.
	 */
	public EntityPlayer create(Player viewer, TabLayoutEntry entry) {
		Objects.requireNonNull(viewer, "Viewer cannot be null");

		Objects.requireNonNull(entry, "Layout entry cannot be null");

		if (entry.isPlayer()) {
			throw new IllegalArgumentException("Real player entries cannot be created as fake players.");
		}

		UUID viewerId = viewer.getUniqueId();
		int position = entry.getPosition();

		if (entryRegistry.contains(viewerId, position)) {
			throw new IllegalStateException("A fake entry is already registered at position " + position
					+ ". Remove it before creating a replacement.");
		}

		String identifier = createIdentifier(viewerId, position);

		GameProfile profile;

		if (entry.isCustomHead()) {

			TabSkinTexture texture = Objects.requireNonNull(entry.getTexture(), "Custom head texture cannot be null");

			profile = profileFactory.createProfile(identifier, texture);

		} else {

			profile = profileFactory.createProfile(identifier);
		}

		// 1. Create the fake player entity.
		EntityPlayer fakePlayer = fakePlayerFactory.create(viewer, profile);

		// 2. Apply the configured visible name.
		displayNameApplier.apply(fakePlayer, entry);

		// 3. Register the prepared entity.
		entryRegistry.register(viewerId, position, fakePlayer);

		return fakePlayer;
	}

	/**
	 * Retrieves a registered fake entry.
	 */
	public EntityPlayer get(UUID viewerId, int position) {
		return entryRegistry.get(viewerId, position);
	}

	/**
	 * Removes and returns a registered fake entry.
	 */
	public EntityPlayer remove(UUID viewerId, int position) {
		return entryRegistry.remove(viewerId, position);
	}

	/**
	 * Removes all registered fake entries for a viewer.
	 */
	public List<EntityPlayer> clear(UUID viewerId) {
		return entryRegistry.clear(viewerId);
	}

	private String createIdentifier(UUID viewerId, int position) {
		return "viewer-" + viewerId + "-slot-" + position;
	}
}
