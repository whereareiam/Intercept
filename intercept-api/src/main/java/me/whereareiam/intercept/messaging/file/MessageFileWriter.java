package me.whereareiam.intercept.messaging.file;

import me.whereareiam.intercept.model.messaging.document.MessageDocument;

import java.nio.file.Path;

/**
 * Writes message persistence data to persistent storage (filesystem, remote storage, etc.).
 */
public interface MessageFileWriter {
	/**
	 * Persist the provided persistence data at the resolved output path.
	 *
	 * @param relativePath path relative to the messages root (without extension)
	 * @param fileData     serialized message data to write
	 */
	void write(String relativePath, MessageDocument fileData);

	/**
	 * Resolve the absolute path (with format-specific extension) for inspection/testing use.
	 *
	 * @param relativePath relative path provided to {@link #write(String, MessageDocument)}
	 * @return absolute resolved path on disk
	 */
	Path resolvePath(String relativePath);
}