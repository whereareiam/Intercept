package me.whereareiam.intercept.translation;

/**
 * Loads translations from platform-specific sources.
 */
public interface TranslationLoader {
	/**
	 * Load translations and their source metadata.
	 *
	 * @return loaded translation data
	 */
	TranslationData load();

	/**
	 * Remove all translation files from the backing storage, if any.
	 */
	void resetStorage();
}
