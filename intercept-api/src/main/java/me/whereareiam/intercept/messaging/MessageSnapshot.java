package me.whereareiam.intercept.messaging;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
	private final Map<String, MessageEntry> entries;
	private final Map<String, Path> filePaths;
}
