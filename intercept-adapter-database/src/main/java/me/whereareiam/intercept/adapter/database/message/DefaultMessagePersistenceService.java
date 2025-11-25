package me.whereareiam.intercept.adapter.database.message;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.adapter.database.message.coordinator.MessageDownloadCoordinator;
import me.whereareiam.intercept.adapter.database.message.coordinator.MessageUploadCoordinator;
import me.whereareiam.intercept.database.MessagePersistenceService;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.messaging.MessageDataService;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import org.jdbi.v3.core.Jdbi;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DefaultMessagePersistenceService implements MessagePersistenceService {
	private final MessageUploadCoordinator uploadCoordinator;
	private final MessageDownloadCoordinator downloadCoordinator;
	private final MessageDataService messageDataService;
	private final Jdbi jdbi;

	@Override
	public void uploadMessages(MessageSnapshot snapshot) {
		if (snapshot == null) throw new IllegalArgumentException("Snapshot cannot be null");
		if (snapshot.getEntries() == null || snapshot.getEntries().isEmpty() || snapshot.getFilePaths() == null) {
			Logger.debug("No entries to upload");
			return;
		}

		jdbi.useTransaction(handle -> uploadCoordinator.upload(snapshot));
		Logger.info("Uploaded %d entries from %d files", snapshot.getEntries().size(), snapshot.getFilePaths().size());
	}

	@Override
	public MessageSnapshot downloadMessages() {
		messageDataService.resetStorage();

		return jdbi.inTransaction(handle -> {
			MessageSnapshot snapshot = downloadCoordinator.download();
			Logger.info("Downloaded %d entries into %d files", snapshot.getEntries().size(), snapshot.getFilePaths().size());

			return snapshot;
		});
	}
}



