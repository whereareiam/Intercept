package me.whereareiam.intercept.registry;

import me.whereareiam.semantica.translation.TranslationRegistry;

/**
 * Registry for message entries.
 * Delegates to Semantica's TranslationRegistry as the source of truth.
 */
public interface MessageRegistry extends TranslationRegistry {
}
