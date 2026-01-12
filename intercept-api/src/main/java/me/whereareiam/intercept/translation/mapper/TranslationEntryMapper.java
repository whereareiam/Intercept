package me.whereareiam.intercept.translation.mapper;

import me.whereareiam.semantica.model.translation.entry.TranslationEntry;

import java.util.Map;

/**
 * Maps platform-specific document formats to Semantica translation entries.
 * <p>
 * Each platform can implement this interface with their own document type,
 * providing type-safe conversion to the common translation entry format.
 *
 * @param <TDocument> Platform-specific document type (e.g., TranslationDocument, MessageDocument)
 */
public interface TranslationEntryMapper<TDocument> {
/**
 * Maps a platform document to translation entries with qualified keys.
 *
 * @param namespace Namespace for key qualification (e.g., "oraylen", "plugin.id")
 * @param document  Platform-specific document containing translation data
 * @return Map of qualified keys to translation entries ready for registration
 */
Map<String, TranslationEntry> map(String namespace, TDocument document);
}
