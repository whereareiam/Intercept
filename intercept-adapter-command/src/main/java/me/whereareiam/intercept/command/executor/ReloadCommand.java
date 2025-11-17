package me.whereareiam.intercept.command.executor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.commandant.Command;
import me.whereareiam.commandant.model.CommandDefinition;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.Serializer;
import me.whereareiam.intercept.model.config.Commands;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.keystone.Actor;
import me.whereareiam.keystone.model.SerializerContent;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Command that reloads all reloadable components in Intercept.
 * This includes configuration, messages, patterns, and other reloadable resources.
 */
@Singleton
public class ReloadCommand implements Command<Actor> {
	private static final String COMMAND_NAME = "reload";
	private final Provider<Commands> commandsProvider;
	private final Provider<Messages> messagesProvider;
	private final Provider<Set<Reloadable>> reloadablesProvider;

	@Inject
	public ReloadCommand(
			@NotNull Provider<Commands> commandsProvider,
			@NotNull Provider<Messages> messagesProvider,
			@NotNull @Named("reloadables") Provider<Set<Reloadable>> reloadablesProvider
	) {
		this.commandsProvider = commandsProvider;
		this.messagesProvider = messagesProvider;
		this.reloadablesProvider = reloadablesProvider;
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
		Messages.Commands.Reload reload = messagesProvider.get().getCommands().getReload();

		try {
			// Reload all registered reloadable components
			Set<Reloadable> reloadables = reloadablesProvider.get();
			for (Reloadable reloadable : reloadables)
				reloadable.reload();

			reload = messagesProvider.get().getCommands().getReload();

			Component component = Serializer.serialize(sender, reload.getSuccess());
			sender.sendMessage(component);
		} catch (Exception e) {
			Component component = Serializer.serialize(SerializerContent.builder()
					.receiver(sender)
					.message(reload.getError())
					.placeholder("error", e.getMessage())
					.build());
			sender.sendMessage(component);
		}
	}
}

