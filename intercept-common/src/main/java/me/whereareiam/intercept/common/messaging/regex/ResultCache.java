package me.whereareiam.intercept.common.messaging.regex;

import com.google.inject.Provider;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.model.config.Settings;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles caching of regex matching results.
 */
@RequiredArgsConstructor
class ResultCache {
	private final Provider<Settings> settingsProvider;
	private final Map<CacheKey, CachedResult> resultCache = new ConcurrentHashMap<>();

	/**
	 * Get a cached result if available and not expired.
	 */
	Optional<String> get(String text, String locale) {
		Settings settings = settingsProvider.get();
		if (!settings.getPerformance().getRegex().isCacheResults())
			return Optional.empty();

		CacheKey cacheKey = new CacheKey(text, locale);
		CachedResult cached = resultCache.get(cacheKey);
		if (cached != null && !cached.isExpired())
			return Optional.ofNullable(cached.result);

		return Optional.empty();
	}

	/**
	 * Store a result in the cache.
	 */
	void put(String text, String locale, Optional<String> result) {
		Settings settings = settingsProvider.get();
		if (!settings.getPerformance().getRegex().isCacheResults())
			return;

		CacheKey cacheKey = new CacheKey(text, locale);
		long expireTime = System.currentTimeMillis() + (settings.getPerformance().getRegex().getCacheExpireMinutes() * 60 * 1000L);
		resultCache.put(cacheKey, new CachedResult(result.orElse(null), expireTime));

		// Clean up expired entries if cache is too large
		if (resultCache.size() > settings.getPerformance().getRegex().getCacheSize())
			cleanupCache();
	}

	/**
	 * Clean up expired entries from the result cache.
	 */
	private void cleanupCache() {
		long now = System.currentTimeMillis();
		resultCache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
	}

	/**
	 * Clear all cached results.
	 */
	void clear() {
		resultCache.clear();
	}

	/**
	 * Cache key for regex matching results.
	 */
	private record CacheKey(String text, String locale) {
	}

	/**
	 * Cached regex matching result.
	 */
	private record CachedResult(String result, long expireTime) {
		boolean isExpired() {
			return isExpired(System.currentTimeMillis());
		}

		boolean isExpired(long now) {
			return now > expireTime;
		}
	}
}

