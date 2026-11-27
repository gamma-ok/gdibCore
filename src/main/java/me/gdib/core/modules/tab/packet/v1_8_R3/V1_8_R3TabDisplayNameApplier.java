package me.gdib.core.modules.tab.packet.v1_8_R3;

import java.util.Objects;

import org.bukkit.Bukkit;

import me.gdib.core.modules.tab.layout.model.TabLayoutEntry;

import net.minecraft.server.v1_8_R3.EntityPlayer;

public final class V1_8_R3TabDisplayNameApplier {

	private final V1_8_R3TabDisplayNameFactory displayNameFactory;

	public V1_8_R3TabDisplayNameApplier(V1_8_R3TabDisplayNameFactory displayNameFactory) {
		this.displayNameFactory = Objects.requireNonNull(displayNameFactory, "DisplayNameFactory cannot be null");
	}

	/**
	 * Applies the configured display name to a fake player.
	 */
	public void apply(EntityPlayer fakePlayer, TabLayoutEntry entry) {
		ensureMainThread();

		Objects.requireNonNull(fakePlayer, "Fake player cannot be null");

		Objects.requireNonNull(entry, "Layout entry cannot be null");

		if (entry.isPlayer()) {
			throw new IllegalArgumentException(
					"Display names for real players " + "cannot be applied through this class.");
		}

		String displayName = entry.getText();

		if (displayName == null || displayName.isBlank()) {
			fakePlayer.listName = displayNameFactory.createEmpty();
			return;
		}

		fakePlayer.listName = displayNameFactory.create(displayName);
	}

	private void ensureMainThread() {
		if (!Bukkit.isPrimaryThread()) {
			throw new IllegalStateException("Display names must be applied " + "from the main server thread.");
		}
	}
}
