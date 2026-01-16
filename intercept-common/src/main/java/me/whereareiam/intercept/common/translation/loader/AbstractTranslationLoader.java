package me.whereareiam.intercept.common.translation.loader;

import com.google.inject.Provider;
import com.google.inject.name.Named;
import me.whereareiam.configura.node.Node;
import me.whereareiam.configura.node.ObjectNode;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.common.persistence.TranslationFileScanner;
import me.whereareiam.intercept.common.persistence.format.DefaultFormatContext;
import me.whereareiam.intercept.common.util.MessageFormatUtil;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.translation.TranslationData;
import me.whereareiam.intercept.translation.TranslationLoader;
import me.whereareiam.intercept.persistence.format.MessageFormat;
import me.whereareiam.intercept.registry.MessageFormatRegistry;
import me.whereareiam.intercept.registry.ReservedKeyRegistry;
import me.whereareiam.intercept.translation.namespace.NamespaceResolver;
import me.whereareiam.intercept.translation.PlatformNamespaceProvider;
import me.whereareiam.intercept.model.messaging.file.MessageExtensions;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.intercept.persistence.file.TranslationFileCodec;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecRegistry;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecResolver;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Shared translation loader for file-based message storage.
 */
public abstract class AbstractTranslationLoader implements TranslationLoader {
	private final Path messagesPath;
	private final MessageFormatRegistry formatRegistry;
	private final ReservedKeyRegistry reservedKeyRegistry;
	private final NamespaceResolver namespaceResolver;
	private final PlatformNamespaceProvider namespaceProvider;
	private final Provider<Locale> defaultLocaleProvider;
	private final TranslationFileCodecRegistry codecRegistry;
	private final TranslationFileCodecResolver codecResolver;

	protected AbstractTranslationLoader(
			@Named("messagesPath") Path messagesPath,
			MessageFormatRegistry formatRegistry,
			ReservedKeyRegistry reservedKeyRegistry,
			NamespaceResolver namespaceResolver,
			PlatformNamespaceProvider namespaceProvider,
			@Named("defaultLocale") Provider<Locale> defaultLocaleProvider,
			TranslationFileCodecRegistry codecRegistry,
			TranslationFileCodecResolver codecResolver
	) {
		this.messagesPath = messagesPath;
		this.formatRegistry = formatRegistry;
		this.reservedKeyRegistry = reservedKeyRegistry;
		this.namespaceResolver = namespaceResolver;
		this.namespaceProvider = namespaceProvider;
		this.defaultLocaleProvider = defaultLocaleProvider;
		this.codecRegistry = codecRegistry;
		this.codecResolver = codecResolver;
	}

	@Override
	public TranslationData load() {
		if (!Files.exists(messagesPath)) {
			Logger.warn("Messages directory does not exist: %s", messagesPath);
			return new MessageSnapshot(Map.of(), Map.of(), Map.of(), Map.of());
		}

		Map<String, Path> filePathMap = new HashMap<>();
		Map<String, String> fileTypeMap = new HashMap<>();
		Map<String, TranslationEntry> entries = new HashMap<>();
		Map<String, MessageExtensions> extensions = new HashMap<>();

		Set<String> namespaces = namespaceResolver == null
				? Set.of(Constants.Namespace.INTERNAL)
				: namespaceResolver.resolveRuntimeNamespaces();

		MessageFormat defaultFormat = formatRegistry == null ? null : formatRegistry.getDefault().orElse(null);
		MessageFormat localeFormat = formatRegistry == null ? null : formatRegistry.get("LOCALE").orElse(null);
		MessageFormat multiLocaleFormat = formatRegistry == null ? null : formatRegistry.get("MULTI_LOCALE").orElse(null);
		if (defaultFormat == null && localeFormat == null && multiLocaleFormat == null) {
			Logger.warn("No message formats registered.");
			return new MessageSnapshot(Map.of(), Map.of(), Map.of(), Map.of());
		}

		for (String namespace : namespaces) {
			if (!isMessagesPathAllowed(namespace)) continue;
			Path root = resolveNamespacePath(namespace);
			ensureNamespaceRoot(root, namespace);
			TranslationFileScanner scanner = new TranslationFileScanner(resolveExtensionsForNamespace(namespace));
			List<Path> files = scanner.scanDirectory(root);
			Logger.debug("Found %d message files in namespace %s", files.size(), namespace);

			for (Path file : files) {
				MessageFormat format = resolveFormat(
						root,
						file,
						defaultLocaleProvider.get(),
						defaultFormat,
						localeFormat,
						multiLocaleFormat
				);
				if (format == null) {
					Logger.debug("Skipping file with unknown format: %s", file);
					continue;
				}

				try {
					loadFile(file, root, namespace, format, scanner, filePathMap, fileTypeMap, entries, extensions);
				} catch (Exception e) {
					Logger.severe("Failed to load message file %s: %s", file, e.getMessage());
				}
			}
		}

		return new MessageSnapshot(
				Map.copyOf(entries),
				Map.copyOf(filePathMap),
				Map.copyOf(extensions),
				Map.copyOf(fileTypeMap)
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

	protected abstract Map<String, TranslationEntry> loadEntries(String keyPrefix, MessageFileData data);

	protected void processDocument(String keyPrefix, MessageFileData data) {
	}

	private void loadFile(
			Path file,
			Path root,
			String namespace,
			MessageFormat format,
			TranslationFileScanner scanner,
			Map<String, Path> filePathMap,
			Map<String, String> fileTypeMap,
			Map<String, TranslationEntry> entries,
			Map<String, MessageExtensions> extensions
	) {
		Locale defaultLocale = defaultLocaleProvider.get();
		String keyPrefix = scanner.buildKeyPrefix(root, file, format, defaultLocale);
		String namespacedPrefix = me.whereareiam.intercept.util.NamespaceUtil.qualify(namespace, keyPrefix);
		Logger.debug("Loading file: %s with key prefix: %s", file.getFileName(), namespacedPrefix);

		filePathMap.put(namespacedPrefix, file);
		fileTypeMap.put(namespacedPrefix, format.getId());

		ObjectNode rawData = loadRawData(root, file, namespace);
		if (rawData.getValues().isEmpty()) return;

		DefaultFormatContext context = new DefaultFormatContext(
				root,
				file,
				defaultLocale,
				namespace,
				reservedKeyRegistry
		);
		MessageFileData data = format.parse(rawData, context);
		Map<String, TranslationEntry> loadedEntries = loadEntries(namespacedPrefix, data);
		if (loadedEntries != null && !loadedEntries.isEmpty()) {
			entries.putAll(loadedEntries);
		}

		Map<String, MessageExtensions> loadedExtensions = extractExtensions(namespacedPrefix, data);
		if (!loadedExtensions.isEmpty()) {
			extensions.putAll(loadedExtensions);
		}

		processDocument(namespacedPrefix, data);
	}

	private MessageFormat resolveFormat(
			Path root,
			Path file,
			Locale defaultLocale,
			MessageFormat defaultFormat,
			MessageFormat localeFormat,
			MessageFormat multiLocaleFormat
	) {
		if (localeFormat != null) {
			MessageFormatUtil.LocaleMatch match = MessageFormatUtil.detectLocaleFromPath(root, file, defaultLocale);
			if (match != null && match.locale() != null)
				return localeFormat;
		}

		if (defaultFormat != null && !"LOCALE".equalsIgnoreCase(defaultFormat.getId()))
			return defaultFormat;

		if (multiLocaleFormat != null)
			return multiLocaleFormat;

		return defaultFormat;
	}

	private ObjectNode loadRawData(Path root, Path file, String namespace) {
		TranslationFileCodec codec = resolveCodec(root, file, namespace);
		if (codec == null) {
			Logger.warn("No translation file codec available for file %s", file);
			return new ObjectNode();
		}

		try {
			return toObjectNode(codec.read(file));
		} catch (Exception e) {
			Logger.warn("Failed to load message file %s: %s", file, e.getMessage());
			return new ObjectNode();
		}
	}

	private TranslationFileCodec resolveCodec(Path root, Path file, String namespace) {
		if (codecResolver != null) {
			String relativePath = resolveRelativePath(root, file);
			TranslationFileCodec resolved = codecResolver.resolve(namespace, relativePath, null);
			if (resolved != null) return resolved;
		}

		if (codecRegistry != null) {
			String extension = extractExtension(file);
			if (extension != null) {
				TranslationFileCodec resolved = codecRegistry.resolveByExtension(extension, namespace).orElse(null);
				if (resolved != null) return resolved;
			}
			return codecRegistry.getDefault();
		}

		return null;
	}

	private ObjectNode toObjectNode(Map<String, Object> data) {
		Node node = MessageFormatUtil.toNode(data);
		if (node instanceof ObjectNode objectNode) {
			return objectNode;
		}
		return new ObjectNode();
	}

	private List<String> resolveExtensionsForNamespace(String namespace) {
		if (codecRegistry != null) {
			Set<String> extensions = codecRegistry.fileExtensions(namespace);
			if (extensions != null && !extensions.isEmpty()) {
				return List.copyOf(extensions);
			}
			TranslationFileCodec codec = codecRegistry.getDefault();
			if (codec != null && codec.getFileExtensions() != null && !codec.getFileExtensions().isEmpty()) {
				return codec.getFileExtensions();
			}
		}
		return List.of(".yml");
	}

	private String resolveRelativePath(Path root, Path file) {
		if (file == null) return "";
		if (root == null) return file.getFileName() == null ? "" : file.getFileName().toString();
		try {
			Path relative = root.relativize(file);
			return relative == null ? "" : relative.toString().replace('\\', '/');
		} catch (Exception e) {
			return file.getFileName() == null ? "" : file.getFileName().toString();
		}
	}

	private String extractExtension(Path file) {
		if (file == null) return null;
		String name = file.getFileName() != null ? file.getFileName().toString() : file.toString();
		int dot = name.lastIndexOf('.');
		if (dot <= 0 || dot == name.length() - 1) return null;
		return name.substring(dot);
	}

	private Map<String, MessageExtensions> extractExtensions(String keyPrefix, MessageFileData data) {
		if (data == null || data.getEntries().isEmpty()) return Map.of();

		Map<String, MessageExtensions> resolved = new HashMap<>();
		String normalizedPrefix = normalizePrefix(keyPrefix);
		collectExtensions(normalizedPrefix, data.getEntries(), resolved);

		return resolved;
	}

	private void collectExtensions(
			String prefix,
			Map<String, MessageFileData.Node> source,
			Map<String, MessageExtensions> target
	) {
		if (source == null || source.isEmpty()) return;

		for (Map.Entry<String, MessageFileData.Node> rawEntry : source.entrySet()) {
			String key = rawEntry.getKey();
			MessageFileData.Node value = rawEntry.getValue();
			if (value == null) continue;

			String fullKey = buildFullKey(prefix, key);
			if (value instanceof MessageFileData.Entry entryData) {
				MessageExtensions entryExtensions = entryData.getExtensions();
				if (entryExtensions != null && !entryExtensions.isEmpty())
					target.put(fullKey, entryExtensions);

				continue;
			}

			if (value instanceof MessageFileData.Section section)
				collectExtensions(fullKey, section.getEntries(), target);
		}
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

	private Path resolveNamespacePath(String namespace) {
		if (namespaceResolver == null) {
			return messagesPath;
		}
		Path resolved = namespaceResolver.resolveNamespaceRoot(namespace);
		return resolved != null ? resolved : messagesPath;
	}

	private void ensureNamespaceRoot(Path root, String namespace) {
		if (root == null) return;
		try {
			Files.createDirectories(root);
		} catch (IOException e) {
			Logger.warn("Failed to create messages namespace directory %s (%s): %s",
					namespace,
					root,
					e.getMessage());
		}
	}

	private boolean isMessagesPathAllowed(String namespace) {
		if (namespaceProvider == null) return true;
		return namespaceProvider.isMessagesPathNamespaceAllowed(namespace);
	}
}
