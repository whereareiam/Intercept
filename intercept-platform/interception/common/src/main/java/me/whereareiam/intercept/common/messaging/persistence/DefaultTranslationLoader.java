package me.whereareiam.intercept.common.messaging.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Config;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.messaging.TranslationData;
import me.whereareiam.intercept.messaging.TranslationLoader;
import me.whereareiam.intercept.messaging.file.MessageFileLoader;
import me.whereareiam.intercept.model.messaging.document.MessageDocument;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.intercept.platform.interception.messaging.InterceptionMessageDocumentProcessor;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Default file-based translation loader for Intercept message documents.
 */
@Singleton
public class DefaultTranslationLoader implements TranslationLoader {
	private final Path messagesPath;
	private final MessageFileScanner scanner;
	private final MessageFileLoader loader;
	private final InterceptionMessageDocumentProcessor documentProcessor;

	@Inject
	public DefaultTranslationLoader(
			@Named("messagesPath") Path messagesPath,
			MessageFileLoader loader,
			InterceptionMessageDocumentProcessor documentProcessor
	) {
		this.messagesPath = messagesPath;
		this.loader = loader;
		this.documentProcessor = documentProcessor;
		this.scanner = new MessageFileScanner(Config.getDefaultReader().getFormat());
	}

	@Override
	public TranslationData load() {
		if (!Files.exists(messagesPath)) {
			Logger.warn("Messages directory does not exist: %s", messagesPath);
			return new MessageSnapshot(Map.of(), Map.of());
		}

		Map<String, Path> filePathMap = new HashMap<>();
		Map<String, TranslationEntry> entries = new HashMap<>();

		List<Path> files = scanner.scanDirectory(messagesPath);
		Logger.debug("Found %d message files", files.size());

		for (Path file : files) {
			try {
				loadFile(file, filePathMap, entries);
			} catch (Exception e) {
				Logger.severe("Failed to load message file %s: %s", file, e.getMessage());
				e.printStackTrace();
			}
		}

		return new MessageSnapshot(Map.copyOf(entries), Map.copyOf(filePathMap));
	}

	@Override
	public void resetStorage() {
		try {
			if (Files.notExists(messagesPath)) {
				Files.createDirectories(messagesPath);
				return;
			}

			try (Stream<Path> stream = Files.walk(messagesPath)) {
				stream
						.sorted(java.util.Comparator.reverseOrder())
						.filter(path -> !path.equals(messagesPath))
						.forEach(path -> {
							try {
								Files.deleteIfExists(path);
							} catch (IOException e) {
								throw new IllegalStateException("Failed to delete path: " + path, e);
							}
						});
			}
		} catch (IOException e) {
			throw new IllegalStateException("Failed to reset messages directory: " + messagesPath, e);
		}
	}

	private void loadFile(Path file, Map<String, Path> filePathMap, Map<String, TranslationEntry> entries) {
		String keyPrefix = scanner.buildKeyPrefix(messagesPath, file);
		Logger.debug("Loading file: %s with key prefix: %s", file.getFileName(), keyPrefix);

		filePathMap.put(keyPrefix, file);

		MessageDocument data = Config.load(file, MessageDocument.class);
		Map<String, TranslationEntry> loadedEntries = loader.loadFromData(keyPrefix, data);
		if (loadedEntries != null && !loadedEntries.isEmpty())
			entries.putAll(loadedEntries);

		runDocumentProcessors(keyPrefix, data);
	}

	private void runDocumentProcessors(String keyPrefix, MessageDocument data) {
		if (documentProcessor == null) return;
		documentProcessor.process(keyPrefix, data);
	}
}
