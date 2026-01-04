package me.whereareiam.intercept.model.messaging.snapshot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.messaging.TranslationData;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;

import java.nio.file.Path;
import java.util.Map;

/**
 * Snapshot of current message data.
 * Contains all entries and their corresponding file paths.
 * Used for upload, download, backup, and other operations.
 */
@Getter
@RequiredArgsConstructor
public class MessageSnapshot implements TranslationData {
	private final Map<String, TranslationEntry> entries;
	private final Map<String, Path> filePaths;
}
