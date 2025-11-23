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
import me.whereareiam.intercept.messaging.MessageSnapshot;
import me.whereareiam.intercept.model.config.Commands;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.model.config.Persistence;
import me.whereareiam.keystone.Actor;
import me.whereareiam.keystone.model.SerializerContent;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
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
		Messages.Commands.Database.Upload upload = messagesProvider.get().getCommands().getDatabase().getUpload();

		try {
			// Create snapshot from MessageDataService
			MessageSnapshot snapshot = messageDataService.createSnapshot();

			if (snapshot.getEntries().isEmpty()) {
				Component component = Serializer.serialize(sender, upload.getNoMessages());
				sender.sendMessage(component);
				return;
			}

			// Refresh messages in case they were reloaded
			upload = messagesProvider.get().getCommands().getDatabase().getUpload();

			Component uploadComponent = Serializer.serialize(SerializerContent.builder()
					.receiver(sender)
					.message(upload.getUploading())
					.placeholders(Map.of(
							"{entries}", String.valueOf(snapshot.getEntries().size()),
							"{files}", String.valueOf(snapshot.getFilePaths().size())
					))
					.build());
			sender.sendMessage(uploadComponent);

			// Upload to database and measure time
			long startTime = System.currentTimeMillis();
			persistenceService.uploadMessages(snapshot);
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;

			// Refresh messages again
			upload = messagesProvider.get().getCommands().getDatabase().getUpload();

			sender.sendMessage(
					Serializer.serialize(SerializerContent.builder()
							.receiver(sender)
							.message(upload.getSuccess())
							.placeholders(Map.of(
									"{entries}", String.valueOf(snapshot.getEntries().size()),
									"{time}", String.valueOf(duration)
							))
							.build())
			);
		} catch (Exception e) {
			// Refresh messages in case they were reloaded
			upload = messagesProvider.get().getCommands().getDatabase().getUpload();

			sender.sendMessage(
					Serializer.serialize(SerializerContent.builder()
							.receiver(sender)
							.message(upload.getError())
							.placeholder("{error}", e.getMessage())
							.build())
			);
			Logger.severe("Failed to upload messages to database: %s", e.getMessage());
			e.printStackTrace();
		}
	}
}
