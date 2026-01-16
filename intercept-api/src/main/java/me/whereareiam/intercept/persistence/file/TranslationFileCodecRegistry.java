package me.whereareiam.intercept.persistence.file;

import java.util.Optional;
import java.util.Set;

/**
 * Registry for translation file codecs.
 */
public interface TranslationFileCodecRegistry {
	/**
	 * Register a codec with a scope.
	 *
	 * @param codec codec to register
	 * @param scope codec scope
	 * @param setDefault whether to set as default for this scope
	 */
	void register(TranslationFileCodec codec, TranslationFileCodecScope scope, boolean setDefault);

	/**
	 * Register a codec with a scope (not default).
	 *
	 * @param codec codec to register
	 * @param scope codec scope
	 */
	default void register(TranslationFileCodec codec, TranslationFileCodecScope scope) {
		register(codec, scope, false);
	}

	/**
	 * Register a global codec (not default).
	 *
	 * @param codec codec to register
	 */
	default void register(TranslationFileCodec codec) {
		register(codec, TranslationFileCodecScope.global(), false);
	}

	/**
	 * Unregister a codec.
	 *
	 * @param codec codec to unregister
	 */
	void unregister(TranslationFileCodec codec);

	/**
	 * Resolve a codec by id for the given namespace.
	 *
	 * @param id codec id
	 * @param namespace namespace context
	 * @return matching codec if available
	 */
	Optional<TranslationFileCodec> resolveById(String id, String namespace);

	/**
	 * Resolve a codec by file extension for the given namespace.
	 *
	 * @param extension file extension
	 * @param namespace namespace context
	 * @return matching codec if available
	 */
	Optional<TranslationFileCodec> resolveByExtension(String extension, String namespace);

	/**
	 * Get the default codec (may be null if not set).
	 *
	 * @return default codec or null
	 */
	TranslationFileCodec getDefault();

	/**
	 * Get all supported extensions for the given namespace.
	 *
	 * @param namespace namespace context
	 * @return supported extensions
	 */
	Set<String> fileExtensions(String namespace);
}
