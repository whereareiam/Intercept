package me.whereareiam.intercept.command.executor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.commandant.Command;
import me.whereareiam.commandant.CommandRegistrar;
import me.whereareiam.commandant.Help;
import me.whereareiam.commandant.Pagination;
import me.whereareiam.commandant.builder.HelpBuilder;
import me.whereareiam.commandant.model.CommandDefinition;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.Serializer;
import me.whereareiam.intercept.model.config.Commands;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.registry.Registry;
import me.whereareiam.keystone.Actor;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Singleton
public class HelpCommand implements Command<Actor>, Reloadable {
	private static final String COMMAND_NAME = "help";
	private final Provider<Commands> commandsProvider;
	private final Provider<Messages> messagesProvider;
	private final Provider<CommandRegistrar<Actor>> commandRegistrarProvider;

	private HelpBuilder<Actor> helpBuilder;

	@Inject
	public HelpCommand(
			@NotNull Provider<Commands> commandsProvider,
			@NotNull Provider<Messages> messagesProvider,
			@NotNull Provider<CommandRegistrar<Actor>> commandRegistrarProvider,
			@NotNull Registry<Reloadable> reloadableRegistry
	) {
		this.commandsProvider = commandsProvider;
		this.messagesProvider = messagesProvider;
		this.commandRegistrarProvider = commandRegistrarProvider;
		reloadableRegistry.register(this);
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

		// Get page argument (default to 1 if not provided)
		int page = context.getOrDefault("page", 1);
		if (page < 1) page = 1;

		// Get help message
		String helpMessage = getHelpBuilder().build(getFilteredCommands(sender), page);

		// Send formatted message using serializer
		Component component = Serializer.serialize(sender, helpMessage);
		sender.sendMessage(component);
	}

	/**
	 * Gets the help builder, creating it lazily on first use.
	 *
	 * @return The help builder instance
	 */
	@NotNull
	private HelpBuilder<Actor> getHelpBuilder() {
		if (helpBuilder == null) {
			Messages messages = messagesProvider.get();

			helpBuilder = Help.create(
					messages.getCommands().getHelp(),
					messages.getCommands().getArguments(),
					Pagination.create(messages.getCommands().getPagination())
			);
		}
		return helpBuilder;
	}

	/**
	 * Gets all commands filtered by sender's permissions.
	 *
	 * @param sender The command sender
	 * @return Collection of commands the sender has permission to see
	 */
	@NotNull
	private Collection<org.incendo.cloud.Command<Actor>> getFilteredCommands(@NotNull Actor sender) {
		CommandManager<Actor> commandManager = commandRegistrarProvider.get().getCommandManager();

		return commandManager.commands()
				.stream()
				.filter(command -> commandManager.hasPermission(sender, command.commandPermission().permissionString()))
				.collect(Collectors.toList());
	}

	@Override
	public void reload() {
		helpBuilder = null;
	}
}