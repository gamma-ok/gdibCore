package me.gdib.core.kernel;

public interface Module {

    /**
     * Returns the unique name of the module.
     */
    String getName();

    /**
     * Returns the module description.
     */
    String getDescription();

    /**
     * Initializes and enables the module.
     */
    void onEnable() throws Exception;

    /**
     * Disables the module and releases its resources.
     */
    void onDisable();

}
