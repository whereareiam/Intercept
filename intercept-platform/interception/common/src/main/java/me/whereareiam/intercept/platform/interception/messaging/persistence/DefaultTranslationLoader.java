package me.whereareiam.intercept.platform.interception.messaging.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Config;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.util.NamespaceUtil;
import me.whereareiam.intercept.messaging.TranslationData;
import me.whereareiam.intercept.messaging.TranslationLoader;
import me.whereareiam.intercept.platform.interception.messaging.file.MessageFileLoader;
import me.whereareiam.intercept.platform.interception.messaging.MessageDocument;
import me.whereareiam.intercept.model.messaging.file.MapMessageExtensionPayload;
import me.whereareiam.intercept.model.messaging.file.MessageExtensionKey;
import me.whereareiam.intercept.model.messaging.file.MessageExtensions;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.intercept.platform.interception.messaging.InterceptionMessageDocumentProcessor;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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

	private static final MessageExtensionKey<MapMessageExtensionPayload> INTERCEPTION_KEY = new MessageExtensionKey<>("interception", MapMessageExtensionPayload.class);

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
			return new MessageSnapshot(Map.of(), Map.of(), Map.of());
		}

		Map<String, Path> filePathMap = new HashMap<>();
		Map<String, TranslationEntry> entries = new HashMap<>();
		Map<String, MessageExtensions> extensions = new HashMap<>();

		List<Path> files = scanner.scanDirectory(messagesPath);
		Logger.debug("Found %d message files", files.size());

		for (Path file : files) {
			try {
				loadFile(file, filePathMap, entries, extensions);
			} catch (Exception e) {
				Logger.severe("Failed to load message file %s: %s", file, e.getMessage());
			}
		}

		return new MessageSnapshot(
				Map.copyOf(entries),
				Map.copyOf(filePathMap),
				Map.copyOf(extensions)
		);
	}

	@Override
	public void resetStorage() {
		try {
			if (Files.notExists(messagesPath)) {
				Files.createDirectories(messagesPath);
				return;
			}

			try (Stream<Path> stream = Files.walk(messagesPath)) {
				stream.sorted(Comparator.reverseOrder())
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

	private void loadFile(
			Path file,
			Map<String, Path> filePathMap,
			Map<String, TranslationEntry> entries,
			Map<String, MessageExtensions> extensions
	) {
		String keyPrefix = scanner.buildKeyPrefix(messagesPath, file);
		String namespacedPrefix = NamespaceUtil.qualify(Constants.Namespace.INTERNAL, keyPrefix);
		Logger.debug("Loading file: %s with key prefix: %s", file.getFileName(), namespacedPrefix);

		filePathMap.put(namespacedPrefix, file);

		MessageDocument data = Config.load(file, MessageDocument.class);
		Map<String, TranslationEntry> loadedEntries = loader.loadFromData(namespacedPrefix, data);
		if (loadedEntries != null && !loadedEntries.isEmpty())
			entries.putAll(loadedEntries);

		Map<String, MessageExtensions> loadedExtensions = extractExtensions(namespacedPrefix, data);
		if (!loadedExtensions.isEmpty())
			extensions.putAll(loadedExtensions);

		runDocumentProcessors(namespacedPrefix, data);
	}

	private void runDocumentProcessors(String keyPrefix, MessageDocument data) {
		if (documentProcessor == null) return;
		documentProcessor.process(keyPrefix, data);
	}

	private Map<String, MessageExtensions> extractExtensions(String keyPrefix, MessageDocument data) {
		if (data == null || data.getEntries().isEmpty()) return Map.of();

		Map<String, MessageExtensions> resolved = new HashMap<>();
		String normalizedPrefix = normalizePrefix(keyPrefix);
		collectExtensions(normalizedPrefix, data.getEntries(), resolved);

		return resolved;
	}

	private void collectExtensions(
			String prefix,
			Map<String, MessageDocument.Node> source,
			Map<String, MessageExtensions> target
	) {
		if (source == null || source.isEmpty()) return;

		for (Map.Entry<String, MessageDocument.Node> rawEntry : source.entrySet()) {
			String key = rawEntry.getKey();
			MessageDocument.Node value = rawEntry.getValue();
			if (value == null) continue;

			String fullKey = buildFullKey(prefix, key);
			if (value instanceof MessageDocument.Entry entryData) {
				MapMessageExtensionPayload payload = toInterceptionPayload(entryData.getInterception());
				if (payload != null) {
					MessageExtensions extensions = new MessageExtensions();
					extensions.put(INTERCEPTION_KEY, payload);
					target.put(fullKey, extensions);
				}
				continue;
			}

			if (value instanceof MessageDocument.Section section)
				collectExtensions(fullKey, section.getEntries(), target);
		}
	}

	private MapMessageExtensionPayload toInterceptionPayload(MessageDocument.Interception interception) {
		if (interception == null || interception.getPatterns() == null || interception.getPatterns().isEmpty())
			return null;

		List<Map<String, Object>> patterns = new ArrayList<>();
		for (MessageDocument.Regex regex : interception.getPatterns()) {
			if (regex == null || regex.getPattern() == null || regex.getPattern().isBlank()) continue;
			Map<String, Object> pattern = new LinkedHashMap<>();
			pattern.put("pattern", regex.getPattern());
			pattern.put("priority", regex.getPriority());
			pattern.put("replaceMatched", regex.isReplaceMatched());
			if (regex.getPlaceholders() != null && !regex.getPlaceholders().isEmpty())
				pattern.put("placeholders", new LinkedHashMap<>(regex.getPlaceholders()));

			patterns.add(pattern);
		}

		if (patterns.isEmpty()) return null;
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("patterns", patterns);

		return new MapMessageExtensionPayload("interception", data);
	}

	private String normalizePrefix(String keyPrefix) {
		if (keyPrefix == null || keyPrefix.isBlank()) return "";

		return keyPrefix.endsWith(".")
				? keyPrefix.substring(0, keyPrefix.length() - 1)
				: keyPrefix;
	}

	private String buildFullKey(String prefix, String key) {
		String escapedKey = escapeSegment(key);
		if (prefix == null || prefix.isEmpty()) return escapedKey;
		if (prefix.endsWith(":")) return prefix + escapedKey;

		return prefix + "." + escapedKey;
	}

	private String escapeSegment(String segment) {
		if (segment == null || segment.isEmpty()) return segment;

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < segment.length(); i++) {
			char c = segment.charAt(i);
			if (c == '\\' || c == '.') builder.append('\\');
			builder.append(c);
		}

		return builder.toString();
	}
}
