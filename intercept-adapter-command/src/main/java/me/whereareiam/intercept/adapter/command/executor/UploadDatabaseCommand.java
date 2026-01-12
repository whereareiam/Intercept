package me.whereareiam.intercept.adapter.command.executor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.commandant.annotation.Definition;
import me.whereareiam.intercept.util.Serializer;
import me.whereareiam.intercept.database.MessagePersistenceService;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.persistence.TranslationDataService;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.keystone.Actor;
import me.whereareiam.keystone.model.SerializerContent;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Command that uploads translations to the database.
 * Only available when database is enabled.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UploadDatabaseCommand {
	private final Provider<Messages> messagesProvider;
	private final MessagePersistenceService persistenceService;
	private final TranslationDataService translationDataService;

	@Definition("database-upload")
	@Command("intercept database upload")
	public void command(@NotNull Actor sender) {
		Messages.Commands.Database.Upload upload = messagesProvider.get().getCommands().getDatabase().getUpload();

		try {
			// Create snapshot from TranslationDataService
			MessageSnapshot snapshot = translationDataService.createSnapshot();

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
							"entries", String.valueOf(snapshot.getEntries().size()),
							"files", String.valueOf(snapshot.getFilePaths().size())
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
									"entries", String.valueOf(snapshot.getEntries().size()),
									"time", String.valueOf(duration)
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
							.placeholder("error", e.getMessage())
							.build())
			);
			Logger.severe("Failed to upload messages to database: %s", e.getMessage());
		}
	}
}
