package me.whereareiam.intercept.common.messaging.cache;

import me.whereareiam.intercept.Reloadable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Multi-level cache for messages.
 * - L1 (STATIC): Never expires, unlimited size
 * - L2 (SEMI_STATIC): Limited size, longer expiry
 * - L3 (DYNAMIC): Small size, short expiry
 */
public class MessageCache implements Reloadable {
	private final Map<CacheKey, String> staticCache;
	private final Map<CacheKey, CacheEntry> semiStaticCache;
	private final Map<CacheKey, CacheEntry> dynamicCache;

	private final int semiStaticMaxSize;
	private final int dynamicMaxSize;
	private final long semiStaticExpireMillis;
	private final long dynamicExpireMillis;

	public MessageCache(int semiStaticMaxSize, int dynamicMaxSize, int semiStaticExpireMinutes, int dynamicExpireMinutes) {
		this.staticCache = new ConcurrentHashMap<>();
		this.semiStaticCache = new ConcurrentHashMap<>();
		this.dynamicCache = new ConcurrentHashMap<>();

		this.semiStaticMaxSize = semiStaticMaxSize;
		this.dynamicMaxSize = dynamicMaxSize;
		this.semiStaticExpireMillis = semiStaticExpireMinutes * 60L * 1000L;
		this.dynamicExpireMillis = dynamicExpireMinutes * 60L * 1000L;
	}

	/**
	 * Get a cached message.
	 *
	 * @param key   the cache key
	 * @param level the cache level
	 * @return cached text, or null if not found or expired
	 */
	public String get(CacheKey key, CacheLevel level) {
		return switch (level) {
			case STATIC -> staticCache.get(key);
			case SEMI_STATIC -> getFromTimedCache(semiStaticCache, key, semiStaticExpireMillis);
			case DYNAMIC -> getFromTimedCache(dynamicCache, key, dynamicExpireMillis);
		};
	}

	/**
	 * Put a message in the cache.
	 *
	 * @param key   the cache key
	 * @param text  the resolved text
	 * @param level the cache level
	 */
	public void put(CacheKey key, String text, CacheLevel level) {
		switch (level) {
			case STATIC -> staticCache.put(key, text);
			case SEMI_STATIC -> putInTimedCache(semiStaticCache, key, text, semiStaticMaxSize);
			case DYNAMIC -> putInTimedCache(dynamicCache, key, text, dynamicMaxSize);
		}
	}

	/**
	 * Clear all caches.
	 */
	public void clear() {
		staticCache.clear();
		semiStaticCache.clear();
		dynamicCache.clear();
	}

	/**
	 * Clear a specific cache level.
	 *
	 * @param level the cache level to clear
	 */
	public void clearLevel(CacheLevel level) {
		switch (level) {
			case STATIC -> staticCache.clear();
			case SEMI_STATIC -> semiStaticCache.clear();
			case DYNAMIC -> dynamicCache.clear();
		}
	}

	private String getFromTimedCache(Map<CacheKey, CacheEntry> cache, CacheKey key, long expireMillis) {
		CacheEntry entry = cache.get(key);
		if (entry == null) {
			return null;
		}

		// Check if expired
		if (System.currentTimeMillis() - entry.timestamp > expireMillis) {
			cache.remove(key);
			return null;
		}

		return entry.text;
	}

	private void putInTimedCache(Map<CacheKey, CacheEntry> cache, CacheKey key, String text, int maxSize) {
		// Simple size management: if over limit, clear oldest entries
		if (cache.size() >= maxSize) {
			// Clear 20% of cache
			int toRemove = maxSize / 5;
			cache.keySet().stream()
					.limit(toRemove)
					.forEach(cache::remove);
		}

		cache.put(key, new CacheEntry(text, System.currentTimeMillis()));
	}

	@Override
	public void reload() {
		clear();
	}

	private record CacheEntry(String text, long timestamp) {
	}
}