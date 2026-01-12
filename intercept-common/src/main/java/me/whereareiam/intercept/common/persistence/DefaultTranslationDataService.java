package me.whereareiam.intercept.common.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.registry.DefaultMessageRegistry;
import me.whereareiam.intercept.common.translation.NamespacedTranslationService;
import me.whereareiam.intercept.persistence.TranslationDataService;
import me.whereareiam.intercept.registry.MessageRegistry;
import me.whereareiam.intercept.translation.TranslationData;
import me.whereareiam.intercept.translation.TranslationLoader;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.intercept.model.messaging.file.MessageExtensions;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default implementation of TranslationDataService.
 * Central service for message file operations and data access.
 * Handles loading and tracking of message entries.
 * Provides unified access to message entries and their file paths.
 */
@Singleton
public class DefaultTranslationDataService implements TranslationDataService, Reloadable {
	private final MessageRegistry registry;
	private final TranslationLoader translationLoader;
	private final NamespacedTranslationService translationService;
	private volatile TranslationData lastData;

	@Inject
	public DefaultTranslationDataService(
			MessageRegistry registry,
			TranslationLoader translationLoader,
			NamespacedTranslationService translationService,
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
			translationService.register(unescapeEntries(entries));
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
		TranslationData data = lastData;
		Map<String, TranslationEntry> entries = data == null
				? Map.of()
				: data.getEntries();

		Map<String, Path> filePaths = getFilePaths();
		Map<String, MessageExtensions> extensions = data == null
				? Map.of()
				: data.getExtensions();

		Map<String, String> fileTypes = data == null
				? Map.of()
				: data.getFileTypes();

		return new MessageSnapshot(entries, filePaths, extensions, fileTypes);
	}

	private Map<String, TranslationEntry> unescapeEntries(Map<String, TranslationEntry> entries) {
		if (entries == null || entries.isEmpty()) return Map.of();

		Map<String, TranslationEntry> resolved = new LinkedHashMap<>();
		for (Map.Entry<String, TranslationEntry> entry : entries.entrySet()) {
			String key = entry.getKey();
			TranslationEntry value = entry.getValue();

			if (key == null || value == null) continue;
			resolved.put(unescapeKey(key), value);
		}

		return resolved;
	}

	private String unescapeKey(String key) {
		if (key == null || key.isEmpty()) return key;

		StringBuilder builder = new StringBuilder();
		boolean escape = false;

		for (int i = 0; i < key.length(); i++) {
			char c = key.charAt(i);
			if (escape) {
				builder.append(c);
				escape = false;
				continue;
			}
			if (c == '\\') {
				escape = true;
				continue;
			}
			builder.append(c);
		}

		if (escape) builder.append('\\');

		return builder.toString();
	}

	@Override
	public void reload() {
		// Clear namespace from translation service
		translationService.unregisterByNamespace(Constants.Namespace.INTERNAL);

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
