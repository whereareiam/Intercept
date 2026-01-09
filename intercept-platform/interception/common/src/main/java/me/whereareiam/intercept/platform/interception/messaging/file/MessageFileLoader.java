package me.whereareiam.intercept.platform.interception.messaging.file;

import me.whereareiam.intercept.platform.interception.messaging.MessageDocument;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;

import java.util.Map;

/**
 * Loads message files into the messaging system.
 * Converts persistence data into Semantica translations and interception rules.
 */
public interface MessageFileLoader {
	/**
	 * Load messages from parsed persistence data.
	 *
	 * @param keyPrefix the key prefix for this persistence (e.g., "errors.permissions")
	 * @param fileData  the parsed persistence data
	 */
	Map<String, TranslationEntry> loadFromData(String keyPrefix, MessageDocument fileData);
}
