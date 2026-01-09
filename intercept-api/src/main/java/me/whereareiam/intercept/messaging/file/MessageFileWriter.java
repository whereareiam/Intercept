package me.whereareiam.intercept.messaging.file;

import me.whereareiam.intercept.model.messaging.file.MessageFileData;

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
	void write(String relativePath, MessageFileData fileData);

	/**
	 * Persist the provided persistence data under a namespace.
	 *
	 * @param namespace    message namespace
	 * @param relativePath path relative to the messages root (without extension)
	 * @param fileData     serialized message data to write
	 */
	default void write(String namespace, String relativePath, MessageFileData fileData) {
		write(relativePath, fileData);
	}

	/**
	 * Resolve the absolute path (with format-specific extension) for inspection/testing use.
	 *
	 * @param relativePath relative path provided to {@link #write(String, MessageFileData)}
	 * @return absolute resolved path on disk
	 */
	Path resolvePath(String relativePath);

	/**
	 * Resolve the absolute path (with format-specific extension) for inspection/testing use.
	 *
	 * @param namespace    message namespace
	 * @param relativePath relative path provided to {@link #write(String, MessageFileData)}
	 * @return absolute resolved path on disk
	 */
	default Path resolvePath(String namespace, String relativePath) {
		return resolvePath(relativePath);
	}
}
