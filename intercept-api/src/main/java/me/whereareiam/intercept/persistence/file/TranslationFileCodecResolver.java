package me.whereareiam.intercept.persistence.file;

/**
 * Resolves the file codec to use for writing translation files.
 */
public interface TranslationFileCodecResolver {
	/**
	 * Resolve a codec for a given namespace and relative path.
	 *
	 * @param namespace namespace (may be null)
	 * @param relativePath relative path without extension
	 * @param fileTypeId explicit file type id (optional)
	 * @return resolved codec or null if not available
	 */
	TranslationFileCodec resolve(String namespace, String relativePath, String fileTypeId);
}
