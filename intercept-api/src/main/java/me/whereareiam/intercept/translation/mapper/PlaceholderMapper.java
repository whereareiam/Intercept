package me.whereareiam.intercept.translation.mapper;

import java.util.Map;

/**
 * Maps platform-specific placeholders into the translation placeholder format.
 *
 * @param <TSource> platform-specific placeholder representation
 */
public interface PlaceholderMapper<TSource> {
	/**
	 * Converts placeholders to a map understood by the translation service.
	 *
	 * @param source the platform-specific placeholders
	 * @return the converted placeholder map
	 */
	Map<String, Object> map(TSource source);
}
