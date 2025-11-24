package me.whereareiam.intercept.command.executor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.commandant.Command;
import me.whereareiam.commandant.model.CommandDefinition;
import me.whereareiam.intercept.Serializer;
import me.whereareiam.intercept.database.MessagePersistenceService;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.messaging.MessageDataService;
import me.whereareiam.intercept.model.config.Commands;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.model.config.Persistence;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.keystone.Actor;
import me.whereareiam.keystone.model.SerializerContent;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Command that downloads translations from the database.
 * Only available when database is enabled.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DownloadDatabaseCommand implements Command<Actor> {
	private static final String COMMAND_NAME = "database-download";
	private final Provider<Commands> commandsProvider;
	private final Provider<Messages> messagesProvider;
	private final Provider<Persistence> persistenceProvider;
	private final MessagePersistenceService persistenceService;
	private final MessageDataService messageDataService;

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
		Messages.Commands.Database.Download download = messagesProvider.get()
				.getCommands()
				.getDatabase()
				.getDownload();

		try {
			if (download != null && download.getPreparing() != null) {
				sender.sendMessage(Serializer.serialize(SerializerContent.builder()
						.receiver(sender)
						.message(download.getPreparing())
						.build()));
			}

			long startTime = System.currentTimeMillis();
			MessageSnapshot snapshot = persistenceService.downloadMessages();
			messageDataService.reload();

			if (snapshot.getEntries().isEmpty()) {
				download = messagesProvider.get().getCommands().getDatabase().getDownload();
				if (download != null && download.getNoEntries() != null) {
					Component component = Serializer.serialize(SerializerContent.builder()
							.receiver(sender)
							.message(download.getNoEntries())
							.build());
					sender.sendMessage(component);
				}
				return;
			}

			long duration = System.currentTimeMillis() - startTime;

			download = messagesProvider.get().getCommands().getDatabase().getDownload();
			if (download != null && download.getSuccess() != null) {
				Component success = Serializer.serialize(SerializerContent.builder()
						.receiver(sender)
						.message(download.getSuccess())
						.placeholders(Map.of(
								"{files}", String.valueOf(snapshot.getFilePaths().size()),
								"{entries}", String.valueOf(snapshot.getEntries().size()),
								"{time}", String.valueOf(duration)
						))
						.build());
				sender.sendMessage(success);
			}
		} catch (Exception e) {
			download = messagesProvider.get().getCommands().getDatabase().getDownload();
			if (download != null && download.getError() != null) {
				Component error = Serializer.serialize(SerializerContent.builder()
						.receiver(sender)
						.message(download.getError())
						.placeholder("{error}", e.getMessage())
						.build());
				sender.sendMessage(error);
			}

			Logger.severe("Failed to download messages from database: %s", e.getMessage());
			e.printStackTrace();
		}
	}
}
