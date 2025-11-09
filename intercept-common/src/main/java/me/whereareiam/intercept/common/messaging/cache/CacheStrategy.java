package me.whereareiam.intercept.common.messaging.cache;

import me.whereareiam.intercept.common.util.MessageTags;

/**
 * Determines what cache level to use for messages.
 */
public class CacheStrategy {
	/**
	 * Classify a message text to determine the appropriate cache level.
	 *
	 * @param text the message text to analyze
	 * @return the cache level
	 */
	public CacheLevel classify(String text) {
		if (text == null || text.isEmpty()) return CacheLevel.STATIC;

		// Check for dynamic placeholders
		if (containsPlaceholders(text)) return CacheLevel.DYNAMIC;

		// Check for conditionals (without placeholders = semi-static)
		if (text.contains(MessageTags.CONDITIONAL_IF_TAG)) return CacheLevel.SEMI_STATIC;

		// No placeholders, no conditionals = static
		// (references and templates with static params are static)
		return CacheLevel.STATIC;
	}

	private boolean containsPlaceholders(String text) {
		// Simple check: does it contain <p:...>?
		return text.contains(MessageTags.PLACEHOLDER_TAG);
	}
}

