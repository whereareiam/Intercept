package me.whereareiam.intercept.messaging.file;

import me.whereareiam.intercept.model.messaging.document.MessageDocument;

/**
 * Loads message files into the MessageRegistry.
 * Converts persistence data into CompiledMessageEntry instances and registers them.
 */
public interface MessageFileLoader {
	/**
	 * Load messages from parsed persistence data into the registry or backing store.
	 *
	 * @param keyPrefix the key prefix for this persistence (e.g., "errors.permissions")
	 * @param fileData  the parsed persistence data
	 */
	void loadFromData(String keyPrefix, MessageDocument fileData);
}