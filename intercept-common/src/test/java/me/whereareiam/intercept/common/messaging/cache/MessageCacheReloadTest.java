package me.whereareiam.intercept.common.messaging.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MessageCacheReloadTest {

	private MessageCache cache;

	@BeforeEach
	void setUp() {
		cache = new MessageCache(100, 50, 60, 30);
	}

	@Test
	void shouldClearAllCacheLevelsOnReload() {
		CacheKey key1 = new CacheKey("key1", Locale.US, Map.of());
		CacheKey key2 = new CacheKey("key2", Locale.US, Map.of());
		CacheKey key3 = new CacheKey("key3", Locale.US, Map.of());

		// Put entries in all cache levels
		cache.put(key1, "Static message", CacheLevel.STATIC);
		cache.put(key2, "Semi-static message", CacheLevel.SEMI_STATIC);
		cache.put(key3, "Dynamic message", CacheLevel.DYNAMIC);

		// Verify entries exist
		assertEquals("Static message", cache.get(key1, CacheLevel.STATIC));
		assertEquals("Semi-static message", cache.get(key2, CacheLevel.SEMI_STATIC));
		assertEquals("Dynamic message", cache.get(key3, CacheLevel.DYNAMIC));

		// Reload
		cache.reload();

		// All entries should be cleared
		assertNull(cache.get(key1, CacheLevel.STATIC));
		assertNull(cache.get(key2, CacheLevel.SEMI_STATIC));
		assertNull(cache.get(key3, CacheLevel.DYNAMIC));
	}

	@Test
	void shouldAllowRecachingAfterReload() {
		CacheKey key = new CacheKey("key", Locale.US, Map.of());

		// Cache initial value
		cache.put(key, "Original", CacheLevel.STATIC);
		assertEquals("Original", cache.get(key, CacheLevel.STATIC));

		// Reload
		cache.reload();
		assertNull(cache.get(key, CacheLevel.STATIC));

		// Cache new value
		cache.put(key, "Updated", CacheLevel.STATIC);
		assertEquals("Updated", cache.get(key, CacheLevel.STATIC));
	}

	@Test
	void shouldHandleReloadOnEmptyCache() {
		// Reload on empty cache should not throw exception
		assertDoesNotThrow(() -> cache.reload());

		// Cache should still work after reload
		CacheKey key = new CacheKey("key", Locale.US, Map.of());
		cache.put(key, "Message", CacheLevel.STATIC);
		assertEquals("Message", cache.get(key, CacheLevel.STATIC));
	}

	@Test
	void shouldHandleMultipleReloads() {
		CacheKey key = new CacheKey("key", Locale.US, Map.of());

		// Add entry
		cache.put(key, "Message", CacheLevel.STATIC);

		// Multiple reloads
		cache.reload();
		assertNull(cache.get(key, CacheLevel.STATIC));

		cache.reload();
		assertNull(cache.get(key, CacheLevel.STATIC));

		cache.reload();
		assertNull(cache.get(key, CacheLevel.STATIC));
	}

}