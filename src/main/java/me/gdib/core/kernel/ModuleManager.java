package me.gdib.core.kernel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ModuleManager {

    private final Map<String, Module> modules;
    private final Map<String, ModuleState> states;
    private final CoreContext context;

    public ModuleManager(CoreContext context) {
        this.context = Objects.requireNonNull(
            context,
            "CoreContext cannot be null"
        );

        this.modules = new LinkedHashMap<>();
        this.states = new LinkedHashMap<>();
    }

    public void registerModule(Module module) {
        Objects.requireNonNull(module, "Module cannot be null");

        String name = Objects.requireNonNull(
            module.getName(),
            "Module name cannot be null"
        );

        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Module name cannot be empty"
            );
        }

        if (modules.containsKey(name)) {
            throw new IllegalStateException(
                "Module is already registered: " + name
            );
        }

        modules.put(name, module);
        states.put(name, ModuleState.UNLOADED);

        context.getPlugin().getLogger().info(
            "Module registered: " + name
        );
    }

    public void enableModule(String name) throws Exception {
        Module module = modules.get(name);

        if (module == null) {
            throw new IllegalArgumentException(
                "Module is not registered: " + name
            );
        }

        ModuleState currentState = states.get(name);

        if (currentState == ModuleState.ENABLED) {
            return;
        }

        if (currentState == ModuleState.LOADING) {
            throw new IllegalStateException(
                "Module is already loading: " + name
            );
        }

        states.put(name, ModuleState.LOADING);

        try {
            module.onEnable();
            states.put(name, ModuleState.ENABLED);

            context.getPlugin().getLogger().info(
                "Module enabled: " + name
            );

        } catch (Exception exception) {
            states.put(name, ModuleState.FAILED);

            context.getPlugin().getLogger().severe(
                "Failed to enable module: " + name
            );

            throw exception;
        }
    }

    public void disableModule(String name) {
        Module module = modules.get(name);

        if (module == null) {
            throw new IllegalArgumentException(
                "Module is not registered: " + name
            );
        }

        if (states.get(name) != ModuleState.ENABLED) {
            return;
        }

        try {
            module.onDisable();
            states.put(name, ModuleState.DISABLED);

            context.getPlugin().getLogger().info(
                "Module disabled: " + name
            );

        } catch (Exception exception) {
            states.put(name, ModuleState.FAILED);

            context.getPlugin().getLogger().severe(
                "Failed to disable module: " + name
            );

            exception.printStackTrace();
        }
    }

    public void enableAll() {
        for (String name : modules.keySet()) {
            try {
                enableModule(name);
            } catch (Exception exception) {
                context.getPlugin().getLogger().severe(
                    "Could not enable module " + name + ": "
                    + exception.getMessage()
                );
            }
        }
    }

    public void disableAll() {
        List<String> names = new ArrayList<>(modules.keySet());

        Collections.reverse(names);

        for (String name : names) {
            disableModule(name);
        }
    }

    public Optional<Module> getModule(String name) {
        return Optional.ofNullable(modules.get(name));
    }

    public ModuleState getState(String name) {
        if (!states.containsKey(name)) {
            throw new IllegalArgumentException(
                "Module is not registered: " + name
            );
        }

        return states.get(name);
    }

    public Map<String, Module> getModules() {
        return Collections.unmodifiableMap(modules);
    }
}
