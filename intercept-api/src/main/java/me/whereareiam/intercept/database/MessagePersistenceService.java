package me.whereareiam.intercept.database;

import me.whereareiam.intercept.messaging.MessageSnapshot;

/**
 * Service for persisting and retrieving messages from the database.
 * Handles upload and download operations for message translations.
 */
public interface MessagePersistenceService {
	/**
	 * Upload messages from storage to the database.
	 *
	 * @param snapshot the message snapshot containing entries and file paths
	 */
	void uploadMessages(MessageSnapshot snapshot);

	/**
	 * Download messages from the database.
	 *
	 * @return message snapshot containing entries and file paths
	 */
	MessageSnapshot downloadMessages();
}

