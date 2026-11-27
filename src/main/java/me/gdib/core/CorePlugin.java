package me.gdib.core;

import org.bukkit.plugin.java.JavaPlugin;

import me.gdib.core.kernel.CoreContext;
import me.gdib.core.modules.tab.TabModule;

public class CorePlugin extends JavaPlugin {

    private volatile CoreContext context;

    @Override
    public void onEnable() {

        getLogger().info("================================");
        getLogger().info("          gdibCore");
        getLogger().info("================================");

        try {

            getLogger().info("Initializing gdibCore...");

            // 1. Initialize the central context.
            context = new CoreContext(this);

            getLogger().info("Shared services initialized.");
            getLogger().info("Module manager initialized.");

            // 2. Register available modules.
            context.getModuleManager().registerModule(
                    new TabModule()
            );

            getLogger().info("Registered module: TAB");

            // 3. Enable all registered modules.
            context.getModuleManager().enableAll();

            getLogger().info("All modules have been processed.");
            getLogger().info(
                    "gdibCore has been enabled successfully!"
            );

        } catch (Exception exception) {

            getLogger().severe(
                    "An error occurred while initializing gdibCore."
            );

            exception.printStackTrace();

            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {

        getLogger().info("Disabling gdibCore...");

        if (context != null) {

            try {

                context.shutdown();

                getLogger().info(
                        "Core context shut down successfully."
                );

            } catch (Exception exception) {

                getLogger().severe(
                        "An error occurred while shutting down gdibCore."
                );

                exception.printStackTrace();

            } finally {

                context = null;
            }
        }

        getLogger().info("gdibCore has been disabled.");
    }

    public CoreContext getContext() {

        if (context == null) {
            throw new IllegalStateException(
                    "gdibCore has not been initialized."
            );
        }

        return context;
    }
}
