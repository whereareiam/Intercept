package me.whereareiam.intercept.common.messaging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Config;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.messaging.loader.MessageFileData;
import me.whereareiam.intercept.common.messaging.loader.MessageFileLoader;
import me.whereareiam.intercept.common.messaging.loader.MessageFileScanner;
import me.whereareiam.intercept.common.messaging.processor.TextProcessor;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.messaging.MessageDataService;
import me.whereareiam.intercept.messaging.MessageEntry;
import me.whereareiam.intercept.messaging.MessageRegistry;
import me.whereareiam.intercept.messaging.MessageSnapshot;
import me.whereareiam.intercept.registry.Registry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of MessageDataService.
 * Central service for message file operations and data access.
 * Handles scanning, loading, and tracking of message files.
 * Provides unified access to message entries and their file paths.
 */
@Singleton
public class DefaultMessageDataService implements MessageDataService, Reloadable {
	private final Path messagesPath;
	private final MessageRegistry registry;
	private final MessageFileScanner scanner;
	private final MessageFileLoader loader;
	
	// Tracks key prefix -> file path mapping
	private final Map<String, Path> filePathMap = new HashMap<>();

	@Inject
	public DefaultMessageDataService(
			@Named("messagesPath") Path messagesPath,
			MessageRegistry registry,
			Registry<Reloadable> reloadableRegistry
	) {
		this.messagesPath = messagesPath;
		this.registry = registry;
		this.scanner = new MessageFileScanner(Config.getDefaultReader().getFormat());
		
		TextProcessor textProcessor = new TextProcessor();
		this.loader = new MessageFileLoader(textProcessor, (DefaultMessageRegistry) registry);
		
		reloadableRegistry.register(this);
	}

	/**
	 * Initialize the service by scanning and loading all message files.
	 */
	public void initialize() {
		if (!Files.exists(messagesPath)) {
			Logger.warn("Messages directory does not exist: %s", messagesPath);
			return;
		}

		filePathMap.clear();

		// Scan for all message files
		List<Path> files = scanner.scanDirectory(messagesPath);
		Logger.debug("Found %d message files", files.size());

		// Load each file and track file paths
		for (Path file : files) {
			try {
				loadFile(file);
			} catch (Exception e) {
				Logger.severe("Failed to load message file %s: %s", file, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Load a single message file and track its path.
	 *
	 * @param file the file to load
	 */
	private void loadFile(Path file) {
		String keyPrefix = scanner.buildKeyPrefix(messagesPath, file);
		Logger.debug("Loading file: %s with key prefix: %s", file.getFileName(), keyPrefix);

		// Track the file path for this key prefix
		filePathMap.put(keyPrefix, file);

		// Read file data with Configura
		MessageFileData data = Config.load(file, MessageFileData.class);

		// Load into registry
		loader.loadFromData(keyPrefix, data);
	}

	/**
	 * Get all message entries from the registry.
	 *
	 * @return map of key to entry
	 */
	public Map<String, MessageEntry> getAllEntries() {
		return registry.getAllEntries();
	}

	/**
	 * Get the file path map (key prefix -> file path).
	 *
	 * @return map of key prefix to file path
	 */
	public Map<String, Path> getFilePaths() {
		return Map.copyOf(filePathMap);
	}

	/**
	 * Get the file path for a specific key prefix.
	 *
	 * @param keyPrefix the key prefix (e.g., "errors.permissions")
	 * @return the file path, or null if not found
	 */
	public Path getFileForPrefix(String keyPrefix) {
		return filePathMap.get(keyPrefix);
	}

	/**
	 * Create a snapshot of current message data.
	 * Includes all entries and their corresponding file paths.
	 *
	 * @return MessageSnapshot with current data
	 */
	public MessageSnapshot createSnapshot() {
		Map<String, MessageEntry> entries = getAllEntries();
		Map<String, Path> filePaths = getFilePaths();
		return new MessageSnapshot(entries, filePaths);
	}

	@Override
	public void reload() {
		// Clear registry (if it supports clearing)
		if (registry instanceof DefaultMessageRegistry) {
			((DefaultMessageRegistry) registry).reload();
		}
		// Reinitialize
		initialize();
	}
}
