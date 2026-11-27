package me.gdib.core.kernel;

import java.util.Objects;

public abstract class AbstractModule implements Module {

    private final ModuleDescriptor descriptor;

    protected AbstractModule(ModuleDescriptor descriptor) {

        this.descriptor = Objects.requireNonNull(
            descriptor,
            "Module descriptor cannot be null"
        );
    }

    @Override
    public final ModuleDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void onDisable(ModuleContext context) {
        // Optional lifecycle implementation.
    }
}
