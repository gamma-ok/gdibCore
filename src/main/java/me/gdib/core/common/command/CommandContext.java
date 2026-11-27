package me.gdib.core.common.command;

import java.util.Arrays;
import java.util.Objects;

import org.bukkit.command.CommandSender;

public final class CommandContext {

    private final CommandSender sender;
    private final String[] arguments;

    public CommandContext(CommandSender sender, String[] arguments) {
        this.sender = Objects.requireNonNull(
                sender,
                "Command sender cannot be null."
        );

        this.arguments = arguments != null
                ? Arrays.copyOf(arguments, arguments.length)
                : new String[0];
    }

    // =========================================================
    // SENDER
    // =========================================================

    public CommandSender getSender() {
        return sender;
    }

    // =========================================================
    // ARGUMENT ACCESS
    // =========================================================

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

    public boolean hasArgument(int index) {
        return index >= 0 && index < arguments.length;
    }
}
