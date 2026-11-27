package me.gdib.core.modules.tab.packet.v1_8_R3;

import java.util.Objects;

import me.gdib.core.modules.tab.packet.TabLayoutPacketAdapter;

public final class V1_8_R3TabLayoutFactory {

	/**
	 * Creates the complete Minecraft 1.8.8 TAB layout adapter.
	 *
	 * @return configured TAB layout packet adapter
	 */
	public TabLayoutPacketAdapter createAdapter() {

		// 1. Shared packet infrastructure.
		V1_8_R3PacketSender packetSender = new V1_8_R3PacketSender();

		// 2. Fake player profile and entity infrastructure.
		V1_8_R3TabProfileFactory profileFactory = new V1_8_R3TabProfileFactory();

		V1_8_R3FakePlayerFactory fakePlayerFactory = new V1_8_R3FakePlayerFactory();

		V1_8_R3TabEntryRegistry entryRegistry = new V1_8_R3TabEntryRegistry();

		// 3. Display name infrastructure.
		V1_8_R3TabDisplayNameFactory displayNameFactory = new V1_8_R3TabDisplayNameFactory();

		V1_8_R3TabDisplayNameApplier displayNameApplier = new V1_8_R3TabDisplayNameApplier(displayNameFactory);

		// 4. Fake entry manager.
		V1_8_R3TabFakeEntryManager fakeEntryManager = new V1_8_R3TabFakeEntryManager(profileFactory, fakePlayerFactory,
				entryRegistry, displayNameApplier);

		// 5. Player information packet infrastructure.
		V1_8_R3TabPacketFactory tabPacketFactory = new V1_8_R3TabPacketFactory();

		// 6. Fake entry synchronization.
		V1_8_R3TabLayoutSynchronizer layoutSynchronizer = new V1_8_R3TabLayoutSynchronizer(fakeEntryManager,
				tabPacketFactory, packetSender);

		// 7. Team sorting infrastructure.
		V1_8_R3TabSortKeyFactory sortKeyFactory = new V1_8_R3TabSortKeyFactory();

		V1_8_R3TabTeamPacketFactory teamPacketFactory = new V1_8_R3TabTeamPacketFactory(sortKeyFactory);

		V1_8_R3TabTeamManager teamManager = new V1_8_R3TabTeamManager(teamPacketFactory, packetSender);

		// 8. Real and fake player team synchronization.
		V1_8_R3TabLayoutTeamSynchronizer teamSynchronizer = new V1_8_R3TabLayoutTeamSynchronizer(teamManager,
				fakeEntryManager);

		// 9. Complete layout controller.
		V1_8_R3TabLayoutController controller = new V1_8_R3TabLayoutController(layoutSynchronizer, teamSynchronizer,
				teamManager);

		// 10. Public packet adapter.
		return new V1_8_R3TabLayoutPacketAdapter(controller);
	}
}
