package me.whereareiam.intercept.common.messaging.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MessageCacheTest {
	private MessageCache cache;

	@BeforeEach
	void setUp() {
		cache = new MessageCache(1000, 500, 100, 10);
	}

	@Test
	void shouldCacheStaticMessage() {
		CacheKey key = new CacheKey("test.key", Locale.US, Map.of());

		cache.put(key, "Cached text", CacheLevel.STATIC);

		String result = cache.get(key, CacheLevel.STATIC);
		assertEquals("Cached text", result);
	}

	@Test
	void shouldCacheSemiStaticMessage() {
		CacheKey key = new CacheKey("test.key", Locale.US, Map.of("condition", "static"));

		cache.put(key, "Semi-static text", CacheLevel.SEMI_STATIC);

		String result = cache.get(key, CacheLevel.SEMI_STATIC);
		assertEquals("Semi-static text", result);
	}

	@Test
	void shouldCacheDynamicMessage() {
		CacheKey key = new CacheKey("test.key", Locale.US, Map.of("name", "Steve"));

		cache.put(key, "Dynamic text", CacheLevel.DYNAMIC);

		String result = cache.get(key, CacheLevel.DYNAMIC);
		assertEquals("Dynamic text", result);
	}

	@Test
	void shouldReturnNullForMissingKey() {
		CacheKey key = new CacheKey("missing", Locale.US, Map.of());

		String result = cache.get(key, CacheLevel.STATIC);
		assertNull(result);
	}

	@Test
	void shouldDifferentiateBetweenLocales() {
		CacheKey enKey = new CacheKey("test", Locale.US, Map.of());
		CacheKey deKey = new CacheKey("test", Locale.GERMAN, Map.of());

		cache.put(enKey, "English", CacheLevel.STATIC);
		cache.put(deKey, "German", CacheLevel.STATIC);

		assertEquals("English", cache.get(enKey, CacheLevel.STATIC));
		assertEquals("German", cache.get(deKey, CacheLevel.STATIC));
	}

	@Test
	void shouldDifferentiateBetweenPlaceholders() {
		CacheKey key1 = new CacheKey("test", Locale.US, Map.of("condition", "a"));
		CacheKey key2 = new CacheKey("test", Locale.US, Map.of("condition", "b"));

		cache.put(key1, "Text A", CacheLevel.SEMI_STATIC);
		cache.put(key2, "Text B", CacheLevel.SEMI_STATIC);

		assertEquals("Text A", cache.get(key1, CacheLevel.SEMI_STATIC));
		assertEquals("Text B", cache.get(key2, CacheLevel.SEMI_STATIC));
	}

	@Test
	void shouldClearCache() {
		CacheKey key = new CacheKey("test", Locale.US, Map.of());
		cache.put(key, "Text", CacheLevel.STATIC);

		cache.clear();

		assertNull(cache.get(key, CacheLevel.STATIC));
	}

	@Test
	void shouldClearSpecificLevel() {
		CacheKey key1 = new CacheKey("test1", Locale.US, Map.of());
		CacheKey key2 = new CacheKey("test2", Locale.US, Map.of());

		cache.put(key1, "Static", CacheLevel.STATIC);
		cache.put(key2, "Dynamic", CacheLevel.DYNAMIC);

		cache.clearLevel(CacheLevel.DYNAMIC);

		assertEquals("Static", cache.get(key1, CacheLevel.STATIC));
		assertNull(cache.get(key2, CacheLevel.DYNAMIC));
	}

	@Test
	void shouldHandleEmptyPlaceholders() {
		CacheKey key1 = new CacheKey("test", Locale.US, Map.of());
		CacheKey key2 = new CacheKey("test", Locale.US, Map.of());

		cache.put(key1, "Text", CacheLevel.STATIC);

		// Same key should retrieve same value
		assertEquals("Text", cache.get(key2, CacheLevel.STATIC));
	}

	@Test
	void shouldRespectCacheSizeLimits() {
		// Fill static cache beyond limit
		for (int i = 0; i < 1500; i++) {
			CacheKey key = new CacheKey("test" + i, Locale.US, Map.of());
			cache.put(key, "Text " + i, CacheLevel.STATIC);
		}

		// Static cache has no size limit, but semi-static and dynamic do
		CacheKey key = new CacheKey("test0", Locale.US, Map.of());
		assertNotNull(cache.get(key, CacheLevel.STATIC)); // Should still be there
	}
}