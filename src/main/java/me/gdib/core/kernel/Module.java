package me.gdib.core.kernel;

public interface Module {

    ModuleDescriptor getDescriptor();

    default String getName() {
        return getDescriptor().getName();
    }

    default String getDescription() {
        return getDescriptor().getDescription();
    }

    void onEnable(ModuleContext context) throws Exception;

    void onDisable(ModuleContext context);
}
