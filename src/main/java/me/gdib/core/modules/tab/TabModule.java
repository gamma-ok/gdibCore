package me.gdib.core.modules.tab;

import java.util.Objects;

import me.gdib.core.common.placeholder.PlaceholderManager;
import me.gdib.core.common.player.PlayerManager;
import me.gdib.core.common.text.TextFormatter;

import me.gdib.core.kernel.AbstractModule;
import me.gdib.core.kernel.ModuleContext;
import me.gdib.core.kernel.ModuleDescriptor;

import me.gdib.core.modules.tab.animation.TabAnimationLoader;
import me.gdib.core.modules.tab.animation.TabAnimationManager;
import me.gdib.core.modules.tab.animation.TabPlaceholderResolver;

import me.gdib.core.modules.tab.config.HeaderFooterConfig;
import me.gdib.core.modules.tab.config.TabConfig;

import me.gdib.core.modules.tab.headerfooter.HeaderFooterFormatter;
import me.gdib.core.modules.tab.headerfooter.HeaderFooterListener;
import me.gdib.core.modules.tab.headerfooter.HeaderFooterManager;
import me.gdib.core.modules.tab.headerfooter.HeaderFooterService;
import me.gdib.core.modules.tab.headerfooter.HeaderFooterUpdater;

import me.gdib.core.modules.tab.packet.TabPacketAdapter;
import me.gdib.core.modules.tab.packet.v1_8_R3.V1_8_R3TabPacketAdapter;

import me.gdib.core.modules.tab.placeholder.TabPlaceholderRegistrar;

public final class TabModule extends AbstractModule {

	private TabConfig tabConfig;

	private TabAnimationManager animationManager;

	private HeaderFooterConfig headerFooterConfig;
	private HeaderFooterManager headerFooterManager;
	private HeaderFooterFormatter headerFooterFormatter;
	private HeaderFooterService headerFooterService;
	private HeaderFooterUpdater headerFooterUpdater;
	private HeaderFooterListener headerFooterListener;

	private TabPacketAdapter packetAdapter;

	private TabPlaceholderRegistrar placeholderRegistrar;

	public TabModule() {
		super(new ModuleDescriptor("tab", "Custom tab list management system.", java.util.List.of(), true));
	}

	@Override
	public void onEnable(ModuleContext context) throws Exception {

		Objects.requireNonNull(context, "ModuleContext cannot be null");

		context.getPlugin().getLogger().info("Initializing TAB module...");

		// 1. Load general TAB configuration.
		tabConfig = new TabConfig(context);

		if (!tabConfig.isEnabled()) {
			context.getPlugin().getLogger().info("TAB module is disabled in its configuration.");
			return;
		}

		// 2. Obtain shared services.
		PlaceholderManager placeholderManager = context.getService(PlaceholderManager.class);

		PlayerManager playerManager = context.getService(PlayerManager.class);

		// 3. Register TAB internal placeholders.
		placeholderRegistrar = new TabPlaceholderRegistrar(placeholderManager, playerManager);

		placeholderRegistrar.register();

		// 4. Initialize animations.
		animationManager = new TabAnimationManager(context);

		TabAnimationLoader animationLoader = new TabAnimationLoader(context, animationManager);

		animationLoader.load();
		animationManager.startAll();

		// 5. Initialize the packet adapter.
		packetAdapter = new V1_8_R3TabPacketAdapter();

		// 6. Initialize Header and Footer if enabled.
		if (tabConfig.isHeaderFooterEnabled()) {

			headerFooterConfig = new HeaderFooterConfig(context);

			headerFooterManager = new HeaderFooterManager(headerFooterConfig);

			TabPlaceholderResolver placeholderResolver = new TabPlaceholderResolver(animationManager);

			TextFormatter textFormatter = context.getService(TextFormatter.class);

			headerFooterFormatter = new HeaderFooterFormatter(placeholderResolver, placeholderManager, textFormatter);

			headerFooterService = new HeaderFooterService(headerFooterManager, headerFooterFormatter, packetAdapter);

			headerFooterUpdater = new HeaderFooterUpdater(context, headerFooterService, playerManager);

			headerFooterListener = new HeaderFooterListener(context, headerFooterUpdater);

			// 7. Register events and start automatic updates.
			headerFooterListener.register();

			headerFooterUpdater.start(tabConfig.getUpdateInterval());

			// 8. Update players already online.
			headerFooterUpdater.updateAll();
		}

		context.getPlugin().getLogger().info("TAB module initialized successfully.");
	}

	@Override
	public void onDisable(ModuleContext context) {

		// 1. Stop event listeners.
		if (headerFooterListener != null) {
			headerFooterListener.unregister();
			headerFooterListener = null;
		}

		// 2. Stop periodic Header and Footer updates.
		if (headerFooterUpdater != null) {
			headerFooterUpdater.stop();
			headerFooterUpdater = null;
		}

		// 3. Stop animations.
		if (animationManager != null) {
			animationManager.stopAll();
			animationManager = null;
		}

		// 4. Unregister TAB internal placeholders.
		if (placeholderRegistrar != null) {
			placeholderRegistrar.unregister();
			placeholderRegistrar = null;
		}

		// 5. Release component references.
		headerFooterService = null;
		headerFooterFormatter = null;
		headerFooterManager = null;
		headerFooterConfig = null;

		packetAdapter = null;
		tabConfig = null;

		context.getPlugin().getLogger().info("TAB module disabled successfully.");
	}
}
