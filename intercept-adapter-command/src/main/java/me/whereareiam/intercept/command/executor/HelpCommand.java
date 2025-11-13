package me.whereareiam.intercept.command.executor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.commandant.Command;
import me.whereareiam.commandant.model.CommandDefinition;
import me.whereareiam.intercept.model.config.Commands;
import me.whereareiam.keystone.model.Actor;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Singleton
public class HelpCommand implements Command<Actor> {
	private static final String COMMAND_NAME = "help";
	private final Provider<Commands> commandsProvider;

	@Inject
	public HelpCommand(@NotNull Provider<Commands> commandsProvider) {
		this.commandsProvider = commandsProvider;
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
		return this::handleCommand;
	}

	private void handleCommand(@NotNull CommandContext<Actor> context) {
		Actor sender = context.sender();
	}
}