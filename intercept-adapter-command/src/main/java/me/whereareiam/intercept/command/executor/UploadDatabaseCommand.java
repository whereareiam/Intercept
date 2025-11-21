package me.whereareiam.intercept.command.executor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.commandant.Command;
import me.whereareiam.commandant.model.CommandDefinition;
import me.whereareiam.intercept.model.config.Commands;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.model.config.Persistence;
import me.whereareiam.keystone.Actor;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Command that uploads translations to the database.
 * Only available when database is enabled.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UploadDatabaseCommand implements Command<Actor> {
	private static final String COMMAND_NAME = "database-upload";
	private final Provider<Commands> commandsProvider;
	private final Provider<Messages> messagesProvider;
	private final Provider<Persistence> persistenceProvider;

	@Override
	@NotNull
	public CommandDefinition getDefinition() {
		// Only enable this command if database is enabled
		Persistence persistence = persistenceProvider.get();
		if (!persistence.isEnabled()) {
			return CommandDefinition.builder()
					.enabled(false)
					.build();
		}

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

		// TODO: Implement translation upload logic
	}
}
