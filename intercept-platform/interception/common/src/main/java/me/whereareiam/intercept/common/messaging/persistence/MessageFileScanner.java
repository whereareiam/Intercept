package me.whereareiam.intercept.common.messaging.persistence;

import me.whereareiam.configura.type.Format;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Scans message directories and builds key prefixes from directory structure.
 */
public class MessageFileScanner {
	private final String[] supportedExtensions;

	public MessageFileScanner(Format format) {
		this.supportedExtensions = new String[]{format.getExtension()};
	}

	/**
	 * Recursively scan a directory for message files.
	 *
	 * @param directory the root directory to scan
	 * @return list of message file paths, sorted alphabetically
	 */
	public List<Path> scanDirectory(Path directory) {
		if (!Files.exists(directory) || !Files.isDirectory(directory)) {
			return List.of();
		}

		List<Path> files = new ArrayList<>();

		try (Stream<Path> pathStream = Files.walk(directory)) {
			pathStream
					.filter(Files::isRegularFile)
					.filter(this::isMessageFile)
					.filter(this::isNotHidden)
					.sorted(Comparator.comparing(Path::toString))
					.forEach(files::add);
		} catch (IOException e) {
			throw new RuntimeException("Failed to scan directory: " + directory, e);
		}

		return files;
	}

	/**
	 * Build a key prefix from the file's path relative to the root.
	 * Example: /messages/errors/permissions.yml -> "errors.permissions"
	 *
	 * @param root the root messages directory
	 * @param file the message file
	 * @return the key prefix (dot-separated path)
	 */
	public String buildKeyPrefix(Path root, Path file) {
		// Get relative path from root to file
		Path relative = root.relativize(file);

		// Convert to string and process
		String pathString = relative.toString();

		// Remove file extension
		pathString = removeExtension(pathString);

		// Replace path separators with dots
		pathString = pathString.replace('\\', '.').replace('/', '.');

		return pathString;
	}

	private boolean isMessageFile(Path path) {
		String fileName = path.getFileName().toString().toLowerCase();
		for (String ext : supportedExtensions) {
			if (fileName.endsWith(ext)) {
				return true;
			}
		}
		return false;
	}

	private boolean isNotHidden(Path path) {
		try {
			// Check if file name starts with .
			if (path.getFileName().toString().startsWith(".")) {
				return false;
			}
			// Check if marked as hidden by the filesystem
			return !Files.isHidden(path);
		} catch (IOException e) {
			return true; // If we can't determine, include it
		}
	}

	private String removeExtension(String pathString) {
		// Find the last occurrence of a supported extension
		for (String ext : supportedExtensions) {
			if (pathString.toLowerCase().endsWith(ext)) {
				return pathString.substring(0, pathString.length() - ext.length());
			}
		}
		return pathString;
	}
}
