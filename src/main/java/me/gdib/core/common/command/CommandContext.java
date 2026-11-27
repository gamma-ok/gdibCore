package me.gdib.core.common.command;

import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class CommandContext {

    private final CommandSender sender;
    private final String[] arguments;

    public CommandContext(CommandSender sender, String[] arguments) {

        if (sender == null) {
            throw new IllegalArgumentException(
                "Command sender cannot be null."
            );
        }

        this.sender = sender;
        this.arguments = arguments != null
                ? Arrays.copyOf(arguments, arguments.length)
                : new String[0];
    }

    public CommandSender getSender() {
        return sender;
    }

    public String[] getArguments() {
        return Arrays.copyOf(arguments, arguments.length);
    }

    public int getArgumentCount() {
        return arguments.length;
    }

    public String getArgument(int index) {

        if (index < 0 || index >= arguments.length) {
            return null;
        }

        return arguments[index];
    }

    public boolean hasArguments() {
        return arguments.length > 0;
    }
}
