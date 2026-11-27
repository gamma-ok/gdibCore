package me.gdib.core.kernel.event;

import java.util.Objects;

import me.gdib.core.kernel.ModuleState;

public final class ModuleStateChangeEvent {

    private final String moduleName;
    private final ModuleState previousState;
    private final ModuleState newState;

    public ModuleStateChangeEvent(
            String moduleName,
            ModuleState previousState,
            ModuleState newState) {

        this.moduleName = Objects.requireNonNull(
            moduleName,
            "Module name cannot be null"
        );

        this.previousState = Objects.requireNonNull(
            previousState,
            "Previous state cannot be null"
        );

        this.newState = Objects.requireNonNull(
            newState,
            "New state cannot be null"
        );
    }

    public String getModuleName() {
        return moduleName;
    }

    public ModuleState getPreviousState() {
        return previousState;
    }

    public ModuleState getNewState() {
        return newState;
    }
}
