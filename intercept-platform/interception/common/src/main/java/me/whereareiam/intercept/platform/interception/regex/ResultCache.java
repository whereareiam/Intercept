package me.whereareiam.intercept.platform.interception.regex;

import com.google.inject.Provider;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.model.config.Interception;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles caching of regex matching results.
 */
@RequiredArgsConstructor
class ResultCache {
	private final Provider<Interception> settingsProvider;
	private final Map<CacheKey, CachedResult> resultCache = new ConcurrentHashMap<>();

	/**
	 * Get a cached result if available and not expired.
	 */
	Optional<String> get(String text, String locale) {
		Interception settings = settingsProvider.get();
		Interception.RegexSettings regexSettings = settings != null ? settings.getRegex() : null;
		if (regexSettings == null || !regexSettings.isCacheResults())
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
		Interception settings = settingsProvider.get();
		Interception.RegexSettings regexSettings = settings != null ? settings.getRegex() : null;
		if (regexSettings == null || !regexSettings.isCacheResults())
			return;

		int cacheSize = regexSettings.getCacheSize();
		if (cacheSize <= 0)
			return;

		CacheKey cacheKey = new CacheKey(text, locale);
		int expireMinutes = regexSettings.getCacheExpireMinutes();
		long expireTime = expireMinutes > 0
				? System.currentTimeMillis() + (expireMinutes * 60 * 1000L)
				: Long.MAX_VALUE;
		resultCache.put(cacheKey, new CachedResult(result.orElse(null), expireTime));

		// Clean up expired entries if cache is too large
		if (resultCache.size() > cacheSize)
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

