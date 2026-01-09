package me.whereareiam.intercept.messaging;

import me.whereareiam.intercept.model.messaging.file.MessageExtensions;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;

import java.nio.file.Path;
import java.util.Map;

/**
 * Container for loaded translations and their source file paths.
 */
public interface TranslationData {
	/**
	 * Loaded translation entries.
	 *
	 * @return map of translation key to entry
	 */
	Map<String, TranslationEntry> getEntries();

	/**
	 * Source file paths for entries.
	 * Implementations may return an empty map when not backed by files.
	 *
	 * @return map of key prefix to file path
	 */
	Map<String, Path> getFilePaths();

	/**
	 * Optional extension payloads by full translation key.
	 *
	 * @return map of full key to extensions
	 */
	default Map<String, MessageExtensions> getExtensions() {
		return Map.of();
	}
}
