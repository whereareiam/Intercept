package me.whereareiam.intercept.command.executor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.commandant.Command;
import me.whereareiam.commandant.model.CommandDefinition;
import me.whereareiam.intercept.model.config.Commands;
import me.whereareiam.keystone.Actor;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Main command that acts as the root command (e.g., /intercept).
 * When executed without subcommands, it shows the help command.
 */
@Singleton
public class MainCommand implements Command<Actor> {
	private static final String COMMAND_NAME = "main";
	private final Provider<Commands> commandsProvider;
	private final HelpCommand helpCommand;

	@Inject
	public MainCommand(
			@NotNull Provider<Commands> commandsProvider,
			@NotNull HelpCommand helpCommand
	) {
		this.commandsProvider = commandsProvider;
		this.helpCommand = helpCommand;
	}

	@Override
	@NotNull
	public CommandDefinition getDefinition() {
		Commands commands = commandsProvider.get();
		CommandDefinition definition = commands.getCommands().get(COMMAND_NAME);
		if (definition == null)
			return CommandDefinition.builder()
					.enabled(false)
					.build();

		return definition;
	}

	@Override
	@NotNull
	public Consumer<CommandContext<Actor>> getHandler() {
		return context -> helpCommand.getHandler().accept(context);
	}
}