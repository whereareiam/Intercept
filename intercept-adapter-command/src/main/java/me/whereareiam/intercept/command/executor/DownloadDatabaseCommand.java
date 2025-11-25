package me.whereareiam.intercept.command.executor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.commandant.annotation.Definition;
import me.whereareiam.intercept.Serializer;
import me.whereareiam.intercept.database.MessagePersistenceService;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.messaging.MessageDataService;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.keystone.Actor;
import me.whereareiam.keystone.model.SerializerContent;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Command that downloads translations from the database.
 * Only available when database is enabled.
 */
@Singleton
public class DownloadDatabaseCommand {
	private final Provider<Messages> messagesProvider;
	private final MessagePersistenceService persistenceService;
	private final MessageDataService messageDataService;

	@Inject
	public DownloadDatabaseCommand(Provider<Messages> messagesProvider, MessagePersistenceService persistenceService, MessageDataService messageDataService) {
		this.messagesProvider = messagesProvider;
		this.persistenceService = persistenceService;
		this.messageDataService = messageDataService;
	}

	@Definition("database-download")
	@Command("database download")
	public void command(@NotNull Actor sender) {
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
