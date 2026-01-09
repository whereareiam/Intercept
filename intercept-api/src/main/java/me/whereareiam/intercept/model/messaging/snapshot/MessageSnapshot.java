package me.whereareiam.intercept.model.messaging.snapshot;

import lombok.Getter;
import me.whereareiam.intercept.messaging.TranslationData;
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

	public MessageSnapshot(Map<String, TranslationEntry> entries, Map<String, Path> filePaths) {
		this(entries, filePaths, Map.of());
	}

	public MessageSnapshot(Map<String, TranslationEntry> entries, Map<String, Path> filePaths, Map<String, MessageExtensions> extensions) {
		this.entries.putAll(entries);
		this.filePaths.putAll(filePaths);
		this.extensions.putAll(extensions);
	}

	@Override
	public Map<String, MessageExtensions> getExtensions() {
		return extensions;
	}
}
