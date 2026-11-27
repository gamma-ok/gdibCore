package me.gdib.core.modules.tab.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import me.gdib.core.kernel.ModuleContext;
import me.gdib.core.kernel.ModuleConfig;
import me.gdib.core.modules.tab.headerfooter.HeaderFooterDesign;

public final class HeaderFooterConfig {

    private final ModuleConfig moduleConfig;

    private boolean enabled;

    private Map<String, HeaderFooterDesign> designs;

    public HeaderFooterConfig(ModuleContext context) {
        Objects.requireNonNull(
                context,
                "Module context cannot be null"
        );

        this.moduleConfig = context.getModuleConfig();

        this.designs = Collections.emptyMap();

        load();
    }

    public void load() {
        FileConfiguration configuration =
                Objects.requireNonNull(
                        moduleConfig.getConfig(),
                        "TAB configuration cannot be null"
                );

        boolean loadedEnabled = configuration.getBoolean(
                "header-footer.enabled",
                true
        );

        ConfigurationSection designsSection =
                configuration.getConfigurationSection(
                        "header-footer.designs"
                );

        Map<String, HeaderFooterDesign> loadedDesigns =
                new LinkedHashMap<>();

        if (designsSection != null) {

            for (String designName : designsSection.getKeys(false)) {

                ConfigurationSection designSection =
                        designsSection.getConfigurationSection(designName);

                if (designSection == null) {
                    continue;
                }

                String displayCondition =
                        designSection.getString("display-condition");

                var header = designSection.getStringList("header");
                var footer = designSection.getStringList("footer");

                HeaderFooterDesign design = new HeaderFooterDesign(
                        designName,
                        displayCondition,
                        header,
                        footer
                );

                loadedDesigns.put(designName, design);
            }
        }

        if (!loadedDesigns.containsKey("default")) {
            throw new IllegalArgumentException(
                    "Header/Footer configuration must contain a default design."
            );
        }

        this.enabled = loadedEnabled;

        this.designs = Collections.unmodifiableMap(
                loadedDesigns
        );
    }

    public boolean isEnabled() {
        return enabled;
    }

    public HeaderFooterDesign getDefaultDesign() {
        return designs.get("default");
    }

    public HeaderFooterDesign getDesign(String name) {
        Objects.requireNonNull(name, "Design name cannot be null");

        return designs.get(name);
    }

    public Map<String, HeaderFooterDesign> getDesigns() {
        return designs;
    }

    public boolean hasDesign(String name) {
        Objects.requireNonNull(name, "Design name cannot be null");

        return designs.containsKey(name);
    }

    public int getDesignCount() {
        return designs.size();
    }
}
