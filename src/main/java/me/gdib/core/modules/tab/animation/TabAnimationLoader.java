package me.gdib.core.modules.tab.animation;

import java.util.List;
import java.util.Objects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import me.gdib.core.common.config.ConfigManager;
import me.gdib.core.kernel.ModuleContext;

public final class TabAnimationLoader {

    private static final String CONFIG_PATH =
            "modules/tab/animations.yml";

    private final ConfigManager configManager;
    private final TabAnimationManager animationManager;

    public TabAnimationLoader(
            ModuleContext context,
            TabAnimationManager animationManager
    ) {
        Objects.requireNonNull(
                context,
                "Module context cannot be null"
        );

        this.configManager = context.getService(ConfigManager.class);

        this.animationManager = Objects.requireNonNull(
                animationManager,
                "Animation manager cannot be null"
        );
    }

    public int load() {
        FileConfiguration configuration =
                configManager.loadConfig(CONFIG_PATH);

        int loadedAnimations = 0;

        for (String animationName : configuration.getKeys(false)) {

            ConfigurationSection animationSection =
                    configuration.getConfigurationSection(animationName);

            if (animationSection == null) {
                continue;
            }

            long changeInterval = animationSection.getLong(
                    "change-interval",
                    1000L
            );

            List<String> texts = animationSection.getStringList("texts");

            TabAnimation animation = new TabAnimation(
                    animationName,
                    changeInterval,
                    texts
            );

            animationManager.registerAnimation(animation);

            loadedAnimations++;
        }

        return loadedAnimations;
    }
}
