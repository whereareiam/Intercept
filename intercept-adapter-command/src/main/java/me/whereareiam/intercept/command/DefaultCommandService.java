package me.whereareiam.intercept.command;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.commandant.Commandant;
import me.whereareiam.commandant.CommandantKeys;
import me.whereareiam.commandant.ExceptionHandlerRegistrar;
import me.whereareiam.commandant.annotation.Definition;
import me.whereareiam.commandant.model.message.ExceptionMessages;
import me.whereareiam.intercept.CommandService;
import me.whereareiam.intercept.command.definition.CommandDefinitionAdapter;
import me.whereareiam.intercept.command.executor.*;
import me.whereareiam.intercept.command.executor.locale.LocaleCommand;
import me.whereareiam.intercept.command.executor.locale.LocaleTargetCommand;
import me.whereareiam.intercept.command.suggestion.LocaleSuggestionProvider;
import me.whereareiam.intercept.command.suggestion.PlayerSuggestionProvider;
import me.whereareiam.intercept.model.CommandDefinition;
import me.whereareiam.intercept.model.config.Commands;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.keystone.Actor;
import me.whereareiam.keystone.serializer.SerializerEngine;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.incendo.cloud.parser.ParserRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Implementation of DefaultCommandService for managing and registering commands.
 * Initializes all commands at plugin startup and registers exception handlers.
 */
@Singleton
public class DefaultCommandService implements CommandService {
	private final SerializerEngine serializer;
	private final Provider<Messages> messagesProvider;
	private final Provider<Commands> commandsProvider;
	private final Provider<CommandManager<Actor>> commandManagerProvider;
	private final Injector injector;

	@Inject
	public DefaultCommandService(
			@NotNull SerializerEngine serializer,
			@NotNull Provider<Messages> messagesProvider,
			@NotNull Provider<Commands> commandsProvider,
			@NotNull Provider<CommandManager<Actor>> commandManagerProvider,
			@NotNull Injector injector
	) {
		this.messagesProvider = messagesProvider;
		this.commandsProvider = commandsProvider;
		this.serializer = serializer;
		this.commandManagerProvider = commandManagerProvider;
		this.injector = injector;

		initialize();
	}

	public void initialize() {
		CommandManager<Actor> commandManager = commandManagerProvider.get();
		Function<String, CommandDefinition> definitionLookup = this::lookupDefinition;
		
		// Register suggestion providers first using the real command manager
		registerSuggestionProviders(commandManager);

		String rootCommand = resolveRootCommand(definitionLookup);
		CommandDefinitionAdapter definitionAdapter = new CommandDefinitionAdapter();
		AnnotationParser<Actor> parser = createCommandAnnotationParser(commandManager);
		Collection<Command<Actor>> parsedCommands = parseCommands(parser);

		for (Command<Actor> command : parsedCommands) {
			String definitionId = command.commandMeta()
					.optional(CommandantKeys.DEFINITION_ID)
					.orElse(null);
			CommandDefinition definition = definitionId != null ? definitionLookup.apply(definitionId) : null;
			CommandDefinition effectiveDefinition = definition;

			if (definition != null && isSubcommand(definition, rootCommand) && definition.getAliases() != null) {
				effectiveDefinition = definition.toBuilder()
						.aliases(prefixAliases(definition.getAliases(), rootCommand))
						.build();
			}

			Commandant.process(command, commandManager)
					.withDefinition(effectiveDefinition, definitionAdapter)
					.register();
		}

		registerExceptionHandlers(commandManager);
	}

	private CommandDefinition lookupDefinition(@NotNull String key) {
		Commands commands = commandsProvider.get();
		return commands.getCommands().get(key);
	}

	private @NotNull String resolveRootCommand(@NotNull Function<String, CommandDefinition> definitionLookup) {
		CommandDefinition definition = definitionLookup.apply("main");
		if (definition == null || definition.getAliases() == null || definition.getAliases().isEmpty())
			return "intercept";

		return definition.getAliases().getFirst();
	}

	private boolean isSubcommand(@NotNull CommandDefinition definition, @NotNull String rootCommand) {
		String usage = definition.getUsage();
		return !rootCommand.isBlank() && usage != null && usage.contains("{command}");
	}

	private @NotNull List<String> prefixAliases(@NotNull List<String> aliases, @NotNull String rootCommand) {
		if (rootCommand.isBlank()) return aliases;

		return aliases.stream()
				.filter(alias -> alias != null && !alias.isBlank())
				.map(alias -> {
					String trimmed = alias.trim();
					String prefix = rootCommand.trim();
					String lowerTrimmed = trimmed.toLowerCase();
					String lowerPrefix = prefix.toLowerCase();
					if (lowerTrimmed.equals(lowerPrefix) || lowerTrimmed.startsWith(lowerPrefix + " ")) {
						return trimmed;
					}
					return prefix + " " + trimmed;
				})
				.toList();
	}

	/**
	 * Registers suggestion providers using Cloud's AnnotationParser with the real command manager.
	 * This ensures suggestions are registered before commands that reference them.
	 */
	private void registerSuggestionProviders(@NotNull CommandManager<Actor> commandManager) {
		AnnotationParser<Actor> parser = new AnnotationParser<>(commandManager, Actor.class);
		parser.parse(
				injector.getInstance(PlayerSuggestionProvider.class),
				injector.getInstance(LocaleSuggestionProvider.class)
		);
	}

	private @NotNull Collection<Command<Actor>> parseCommands(@NotNull AnnotationParser<Actor> parser) {
		return parser.parse(
				injector.getInstance(MainCommand.class),
				injector.getInstance(HelpCommand.class),
				injector.getInstance(ReloadCommand.class),
				injector.getInstance(InspectCommand.class),
				injector.getInstance(LocaleCommand.class),
				injector.getInstance(LocaleTargetCommand.class),
				injector.getInstance(UploadDatabaseCommand.class),
				injector.getInstance(DownloadDatabaseCommand.class)
		);
	}

	private void registerExceptionHandlers(@NotNull CommandManager<Actor> commandManager) {
		ExceptionMessages exceptionMessages = messagesProvider.get().getCommands() != null
				? messagesProvider.get().getCommands().getExceptions()
				: new ExceptionMessages();

		ExceptionHandlerRegistrar.register(commandManager, exceptionMessages, serializer, Actor::getAudience);
	}

	private @NotNull AnnotationParser<Actor> createCommandAnnotationParser(
			@NotNull CommandManager<Actor> commandManager
	) {
		AnnotationParser<Actor> parser = new AnnotationParser<>(
				new RecordingCommandManager<>(commandManager),
				Actor.class
		);

		parser.registerBuilderModifier(
				Definition.class,
				(annotation, builder) -> builder.meta(CommandantKeys.DEFINITION_ID, annotation.value())
		);

		return parser;
	}

	private static final class RecordingCommandManager<C> extends CommandManager<C> {
		private final CommandManager<C> realManager;

		RecordingCommandManager(@NotNull CommandManager<C> realManager) {
			super(ExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler());
			this.realManager = realManager;
		}

		@Override
		public boolean hasPermission(@NotNull C sender, @NotNull String permission) {
			return true;
		}

		@Override
		public @NotNull ParserRegistry<C> parserRegistry() {
			return realManager.parserRegistry();
		}
	}
}
