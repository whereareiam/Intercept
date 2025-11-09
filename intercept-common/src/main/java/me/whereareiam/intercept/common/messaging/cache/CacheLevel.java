package me.whereareiam.intercept.common.messaging.cache;

/**
 * Cache level for message entries.
 */
public enum CacheLevel {
	/**
	 * Fully static - no placeholders, no dynamic conditionals.
	 * Can be cached indefinitely and pre-rendered.
	 */
	STATIC,

	/**
	 * Semi-static - has conditionals or templates but no dynamic placeholders.
	 * Can be cached after the first resolution.
	 */
	SEMI_STATIC,

	/**
	 * Dynamic - has runtime placeholders.
	 * Short-lived cache or no cache.
	 */
	DYNAMIC
}