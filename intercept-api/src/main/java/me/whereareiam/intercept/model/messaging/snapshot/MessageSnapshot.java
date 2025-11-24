package me.whereareiam.intercept.model.messaging.snapshot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.model.messaging.CompiledMessageEntry;

import java.nio.file.Path;
import java.util.Map;

/**
 * Snapshot of current message data.
 * Contains all entries and their corresponding file paths.
 * Used for upload, download, backup, and other operations.
 */
@Getter
@RequiredArgsConstructor
public class MessageSnapshot {
	private final Map<String, CompiledMessageEntry> entries;
	private final Map<String, Path> filePaths;
}
