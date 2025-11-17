package me.whereareiam.intercept.command;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.commandant.Command;
import me.whereareiam.commandant.CommandRegistrar;
import me.whereareiam.commandant.Commandant;
import me.whereareiam.commandant.model.CommandDefinition;
import me.whereareiam.intercept.command.executor.HelpCommand;
import me.whereareiam.intercept.command.executor.InspectCommand;
import me.whereareiam.intercept.command.executor.MainCommand;
import me.whereareiam.intercept.command.executor.ReloadCommand;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.keystone.Actor;
import me.whereareiam.keystone.serializer.SerializerEngine;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

/**
 * Implementation of CommandService for managing and registering commands.
 * Initializes all commands at plugin startup and registers exception handlers.
 */
@Singleton
public class DefaultCommandService implements CommandService {
	private final SerializerEngine serializer;
	private final CommandRegistrar<Actor> registrar;
	private final Provider<Messages> messagesProvider;
	private final Injector injector;

	@SuppressWarnings("unchecked")
	private final Class<Command<Actor>>[] commands = new Class[]{
			HelpCommand.class,
			ReloadCommand.class,
			InspectCommand.class
	};

	@Inject
	public DefaultCommandService(
			@NotNull Provider<CommandRegistrar<Actor>> commandRegistrarProvider,
			@NotNull SerializerEngine serializer,
			@NotNull Provider<Messages> messagesProvider,
			@NotNull Injector injector
	) {
		this.messagesProvider = messagesProvider;
		this.serializer = serializer;
		this.injector = injector;

		registrar = commandRegistrarProvider.get();
		registerExceptionHandlers(registrar);
		initialize();
	}

	public void initialize() {
		// Register MainCommand as root command
		MainCommand mainCommand = injector.getInstance(MainCommand.class);
		CommandDefinition primary = mainCommand.getDefinition();

		if (primary.isEnabled()) {
			registrar.registerCommand(primary, mainCommand.getHandler());

			// Set root command name from first alias
			List<String> aliases = primary.getAliases();
			if (aliases != null && !aliases.isEmpty())
				registrar.setRootCommand(aliases.getFirst());
		}

		// Register all other commands
		Stream.of(commands)
				.map(injector::getInstance)
				.forEach(this::register);
	}

	@Override
	public void register(@NotNull Command<Actor> command) {
		CommandDefinition definition = command.getDefinition();
		if (definition.isEnabled())
			registrar.registerCommand(definition, command.getHandler());
	}

	private void registerExceptionHandlers(@NotNull CommandRegistrar<Actor> registrar) {
		Commandant.registerExceptionHandler(
				messagesProvider.get().getCommands().getExceptions(),
				serializer,
				registrar.getCommandManager(),
				Actor::getAudience
		);
	}
}