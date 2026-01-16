package me.whereareiam.intercept.persistence.file;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Codec for reading and writing translation files of a specific syntax.
 */
public interface TranslationFileCodec {
	/**
	 * Unique codec identifier (e.g., "YAML", "JSON", "PROPERTIES").
	 *
	 * @return codec id
	 */
	String getId();

	/**
	 * Supported file extensions (including dot, e.g., ".yml", ".json").
	 *
	 * @return supported extensions
	 */
	List<String> getFileExtensions();

	/**
	 * Read a file into a nested map structure.
	 *
	 * @param path file path
	 * @return parsed data (never null)
	 * @throws IOException when read fails
	 */
	Map<String, Object> read(Path path) throws IOException;

	/**
	 * Write a nested map structure into a file.
	 *
	 * @param path file path
	 * @param data map data
	 * @throws IOException when write fails
	 */
	void write(Path path, Map<String, Object> data) throws IOException;

	/**
	 * Check whether this codec supports the given extension.
	 *
	 * @param extension file extension (with or without leading dot)
	 * @return true if supported
	 */
	default boolean supportsExtension(String extension) {
		if (extension == null || extension.isBlank()) return false;
		String normalized = extension.startsWith(".") ? extension.toLowerCase(Locale.ROOT) : "." + extension.toLowerCase(Locale.ROOT);
		for (String ext : getFileExtensions()) {
			if (ext == null) continue;
			if (normalized.equals(ext.toLowerCase(Locale.ROOT))) return true;
		}
		return false;
	}
}
