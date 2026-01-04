package me.whereareiam.intercept.common.messaging.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.messaging.DefaultMessageRegistry;
import me.whereareiam.intercept.messaging.MessageDataService;
import me.whereareiam.intercept.messaging.MessageRegistry;
import me.whereareiam.intercept.messaging.TranslationData;
import me.whereareiam.intercept.messaging.TranslationLoader;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.TranslationService;

import java.nio.file.Path;
import java.util.Map;
import java.util.Locale;

/**
 * Default implementation of MessageDataService.
 * Central service for message file operations and data access.
 * Handles loading and tracking of message entries.
 * Provides unified access to message entries and their file paths.
 */
@Singleton
public class DefaultMessageDataService implements MessageDataService, Reloadable {
	private final MessageRegistry registry;
	private final TranslationLoader translationLoader;
	private final TranslationService<Locale> translationService;
	private volatile TranslationData lastData;

	@Inject
	public DefaultMessageDataService(
			MessageRegistry registry,
			TranslationLoader translationLoader,
			TranslationService<Locale> translationService,
			Registry<Reloadable> reloadableRegistry
	) {
		this.registry = registry;
		this.translationLoader = translationLoader;
		this.translationService = translationService;

		reloadableRegistry.register(this);
	}

	/**
	 * Initialize the service by scanning and loading all message files.
	 */
	public void initialize() {
		TranslationData data = translationLoader.load();
		if (data == null) {
			lastData = new MessageSnapshot(Map.of(), Map.of());
			return;
		}

		lastData = data;
		Map<String, TranslationEntry> entries = data.getEntries();
		if (entries != null && !entries.isEmpty()) {
			translationService.register(entries);
		}
	}

	/**
	 * Get all message entries from the registry.
	 *
	 * @return map of key to entry
	 */
	@Override
	public Map<String, TranslationEntry> getAllEntries() {
		return registry.getAllEntries();
	}

	/**
	 * Get the file path map (key prefix -> file path).
	 *
	 * @return map of key prefix to file path
	 */
	@Override
	public Map<String, Path> getFilePaths() {
		TranslationData data = lastData;
		return data == null ? Map.of() : data.getFilePaths();
	}

	/**
	 * Create a snapshot of current message data.
	 * Includes all entries and their corresponding file paths.
	 *
	 * @return MessageSnapshot with current data
	 */
	@Override
	public MessageSnapshot createSnapshot() {
		Map<String, TranslationEntry> entries = getAllEntries();
		Map<String, Path> filePaths = getFilePaths();
		return new MessageSnapshot(entries, filePaths);
	}

	@Override
	public void reload() {
		// Clear registry (if it supports clearing)
		if (registry instanceof DefaultMessageRegistry)
			((DefaultMessageRegistry) registry).reload();

		// Reinitialize
		initialize();
	}

	@Override
	public void resetStorage() {
		translationLoader.resetStorage();
	}
}
