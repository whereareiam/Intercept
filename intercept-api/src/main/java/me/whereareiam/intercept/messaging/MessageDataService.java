package me.whereareiam.intercept.messaging;

import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;

import java.nio.file.Path;
import java.util.Map;

/**
 * Central service for message file operations and data access.
 * Provides unified access to message entries and their file paths.
 */
public interface MessageDataService {
	/**
	 * Initialize the service by scanning and loading all message files.
	 */
	void initialize();

	/**
	 * Get all message entries from the registry.
	 *
	 * @return map of key to entry
	 */
	Map<String, TranslationEntry> getAllEntries();

	/**
	 * Get the file path map (key prefix -> file path).
	 *
	 * @return map of key prefix to file path
	 */
	Map<String, Path> getFilePaths();

	/**
	 * Create a snapshot of current message data.
	 * Includes all entries and their corresponding file paths.
	 *
	 * @return MessageSnapshot with current data
	 */
	MessageSnapshot createSnapshot();

	/**
	 * Reload the service by clearing and reinitializing.
	 */
	void reload();

	/**
	 * Remove all message files from the backing storage.
	 */
	void resetStorage();
}

