package me.gdib.core;

import org.bukkit.plugin.java.JavaPlugin;

public class CorePlugin extends JavaPlugin {

    @Override
    public void onEnable() {

        getLogger().info("================================");
        getLogger().info("       GDIB CORE");
        getLogger().info("================================");

        getLogger().info("Initializing gdibCore...");

        // Kernel initialization will be implemented here.

        getLogger().info("GDIB Core has been enabled successfully!");

    }

    @Override
    public void onDisable() {

        getLogger().info("Disabling gdibCore...");

        // Kernel shutdown will be implemented here.

        getLogger().info("GDIB Core has been disabled.");

    }

}
