package me.whereareiam.intercept.common.messaging.cache;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Cache key for message resolution.
 * Combines message key, locale, and static placeholders.
 */
public record CacheKey(
		String messageKey,
		Locale locale,
		Map<String, Object> staticPlaceholders
) {
	public CacheKey {
		// Ensure placeholders are sorted for consistent equality
		staticPlaceholders = new TreeMap<>(staticPlaceholders);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CacheKey cacheKey)) return false;
		return Objects.equals(messageKey, cacheKey.messageKey) &&
				Objects.equals(locale, cacheKey.locale) &&
				Objects.equals(staticPlaceholders, cacheKey.staticPlaceholders);
	}

	@Override
	public int hashCode() {
		return Objects.hash(messageKey, locale, staticPlaceholders);
	}
}