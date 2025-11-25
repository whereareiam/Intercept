package me.whereareiam.intercept.command.executor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.commandant.annotation.Definition;
import me.whereareiam.keystone.Actor;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;

/**
 * Main command that acts as the root command (e.g., /intercept).
 * When executed without subcommands, it shows the help command.
 */
@Singleton
public class MainCommand {
	private final HelpCommand helpCommand;

	@Inject
	public MainCommand(@NotNull HelpCommand helpCommand) {
		this.helpCommand = helpCommand;
	}

	@Definition("main")
	@Command("intercept")
	public void command(@NotNull Actor sender) {
		helpCommand.command(sender, 1);
	}
}