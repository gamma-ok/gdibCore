package me.gdib.core.common.command;

public interface CoreCommand {

    String getName();

    String getDescription();

    String getUsage();

    CommandResult execute(CommandContext context);
}