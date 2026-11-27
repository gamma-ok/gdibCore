package me.gdib.core.common.command;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class CommandManager implements CommandExecutor {

    private final JavaPlugin plugin;
    private final Map<String, CoreCommand> commands;

    public CommandManager(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "Plugin cannot be null"
        );

        this.commands = new LinkedHashMap<>();
    }

    // =========================================================
    // COMMAND REGISTRATION
    // =========================================================

    public void registerCommand(CoreCommand coreCommand) {
        ensureMainThread();

        Objects.requireNonNull(
                coreCommand,
                "Command cannot be null"
        );

        String name = normalizeName(coreCommand.getName());

        if (name.isEmpty()) {
            throw new IllegalArgumentException(
                    "Command name cannot be empty."
            );
        }

        if (commands.containsKey(name)) {
            throw new IllegalArgumentException(
                    "Command is already registered: " + name
            );
        }

        PluginCommand pluginCommand = plugin.getCommand(name);

        if (pluginCommand == null) {
            throw new IllegalArgumentException(
                    "Command is not declared in plugin.yml: " + name
            );
        }

        pluginCommand.setExecutor(this);
        commands.put(name, coreCommand);

        plugin.getLogger().info(
                "Registered command: /" + name
        );
    }

    public void unregisterCommand(String name) {
        ensureMainThread();

        String normalizedName = normalizeName(name);

        if (commands.remove(normalizedName) != null) {
            plugin.getLogger().info(
                    "Unregistered command: /" + normalizedName
            );
        }
    }

    // =========================================================
    // COMMAND INFORMATION
    // =========================================================

    public CoreCommand getCommand(String name) {
        ensureMainThread();

        return commands.get(normalizeName(name));
    }

    public boolean isRegistered(String name) {
        ensureMainThread();

        return commands.containsKey(normalizeName(name));
    }

    public int getRegisteredCommandCount() {
        ensureMainThread();

        return commands.size();
    }

    // =========================================================
    // COMMAND EXECUTION
    // =========================================================

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] arguments
    ) {
        ensureMainThread();

        String name = normalizeName(command.getName());

        CoreCommand coreCommand = commands.get(name);

        if (coreCommand == null) {
            return true;
        }

        try {
            CommandContext context = new CommandContext(
                    sender,
                    arguments
            );

            CommandResult result = coreCommand.execute(context);

            if (result == null) {
                result = CommandResult.ERROR;
            }

            switch (result) {

                case SUCCESS:
                    return true;

                case USAGE_ERROR:
                    sender.sendMessage(coreCommand.getUsage());
                    return true;

                case NO_PERMISSION:
                    sender.sendMessage(
                            "§cYou do not have permission to execute this command."
                    );
                    return true;

                case ERROR:
                    sender.sendMessage(
                            "§cAn error occurred while executing this command."
                    );
                    return true;

                default:
                    return true;
            }

        } catch (RuntimeException exception) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "An error occurred while executing command: /" + name,
                    exception
            );

            sender.sendMessage(
                    "§cAn unexpected error occurred."
            );

            return true;
        }
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    public void clear() {
        ensureMainThread();

        commands.clear();
    }

    // =========================================================
    // INTERNAL UTILITIES
    // =========================================================

    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }

        return name.toLowerCase(Locale.ROOT);
    }

    private void ensureMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException(
                    "CommandManager operations must be executed "
                            + "on the main server thread."
            );
        }
    }
}
