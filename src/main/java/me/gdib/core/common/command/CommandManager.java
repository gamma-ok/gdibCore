package me.gdib.core.common.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class CommandManager implements CommandExecutor {

    private final JavaPlugin plugin;

    private final Map<String, CoreCommand> commands;

    public CommandManager(JavaPlugin plugin) {

        if (plugin == null) {
            throw new IllegalArgumentException(
                "Plugin cannot be null."
            );
        }

        this.plugin = plugin;
        this.commands = new LinkedHashMap<>();
    }

    public void registerCommand(CoreCommand coreCommand) {

        if (coreCommand == null) {
            throw new IllegalArgumentException(
                "Command cannot be null."
            );
        }

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

        org.bukkit.command.PluginCommand pluginCommand =
                plugin.getCommand(name);

        if (pluginCommand == null) {
            throw new IllegalArgumentException(
                "Command is not declared in plugin.yml: " + name
            );
        }

        commands.put(name, coreCommand);
        pluginCommand.setExecutor(this);

        plugin.getLogger().info(
            "Registered command: /" + name
        );
    }

    public void unregisterCommand(String name) {

        String normalizedName = normalizeName(name);

        if (commands.remove(normalizedName) != null) {

            plugin.getLogger().info(
                "Unregistered command: /" + normalizedName
            );
        }
    }

    public CoreCommand getCommand(String name) {
        return commands.get(normalizeName(name));
    }

    public boolean isRegistered(String name) {
        return commands.containsKey(normalizeName(name));
    }

    public int getRegisteredCommandCount() {
        return commands.size();
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] arguments) {

        String name = normalizeName(command.getName());

        CoreCommand coreCommand = commands.get(name);

        if (coreCommand == null) {
            return false;
        }

        try {

            CommandContext context =
                    new CommandContext(sender, arguments);

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

            plugin.getLogger().severe(
                "An error occurred while executing command: /" + name
            );

            exception.printStackTrace();

            sender.sendMessage(
                "§cAn unexpected error occurred."
            );

            return true;
        }
    }

    private String normalizeName(String name) {

        if (name == null) {
            return "";
        }

        return name.toLowerCase(Locale.ROOT);
    }
    
    public void clear() {
        commands.clear();
    }
}
