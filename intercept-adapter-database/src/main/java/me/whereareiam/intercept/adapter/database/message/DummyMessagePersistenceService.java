package me.whereareiam.intercept.adapter.database.message;

import com.google.inject.Singleton;
import me.whereareiam.intercept.database.MessagePersistenceService;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;

import java.util.Collections;

/**
 * Dummy implementation of MessagePersistenceService used when persistence is disabled.
 * All operations are silent no-ops.
 */
@Singleton
public class DummyMessagePersistenceService implements MessagePersistenceService {
	@Override
	public void uploadMessages(MessageSnapshot snapshot) {
		// No-op
	}

	@Override
	public MessageSnapshot downloadMessages() {
		// Return empty snapshot with empty maps
		return new MessageSnapshot(Collections.emptyMap(), Collections.emptyMap());
	}
}