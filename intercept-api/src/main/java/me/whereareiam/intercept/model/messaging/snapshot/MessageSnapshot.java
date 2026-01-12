package me.whereareiam.intercept.model.messaging.snapshot;

import lombok.Getter;
import me.whereareiam.intercept.translation.TranslationData;
import me.whereareiam.intercept.model.messaging.file.MessageExtensions;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Snapshot of current message data.
 * Contains all entries and their corresponding file paths.
 * Used for upload, download, backup, and other operations.
 */
@Getter
public class MessageSnapshot implements TranslationData {
	private final Map<String, TranslationEntry> entries = new HashMap<>();
	private final Map<String, Path> filePaths = new HashMap<>();
	private final Map<String, MessageExtensions> extensions = new HashMap<>();
	private final Map<String, String> fileTypes = new HashMap<>();

	public MessageSnapshot(Map<String, TranslationEntry> entries, Map<String, Path> filePaths) {
		this(entries, filePaths, Map.of(), Map.of());
	}

	public MessageSnapshot(Map<String, TranslationEntry> entries, Map<String, Path> filePaths, Map<String, MessageExtensions> extensions) {
		this(entries, filePaths, extensions, Map.of());
	}

	public MessageSnapshot(
			Map<String, TranslationEntry> entries,
			Map<String, Path> filePaths,
			Map<String, MessageExtensions> extensions,
			Map<String, String> fileTypes
	) {
		this.entries.putAll(entries);
		this.filePaths.putAll(filePaths);
		this.extensions.putAll(extensions);
		this.fileTypes.putAll(fileTypes);
	}

	@Override
	public Map<String, MessageExtensions> getExtensions() {
		return extensions;
	}

	@Override
	public Map<String, String> getFileTypes() {
		return fileTypes;
	}
}
