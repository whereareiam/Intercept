package me.whereareiam.intercept.messaging;

import java.util.Map;
import java.util.Set;

/**
 * Registry for message entries.
 * Manages registration and retrieval of localized message entries by key.
 */
public interface MessageRegistry {
	/**
	 * Register a message entry with a key.
	 *
	 * @param key   the message key
	 * @param entry the message entry
	 */
	void register(String key, MessageEntry entry);
	/**
	 * Get a message entry by key.
	 *
	 * @param key the message key
	 * @return the message entry, or null if not found
	 */
	MessageEntry get(String key);

	/**
	 * Get all registered keys.
	 *
	 * @return set of all message keys
	 */
	Set<String> getKeys();

	/**
	 * Get keys matching a prefix.
	 *
	 * @param prefix the key prefix (e.g., "errors.permissions")
	 * @return set of matching keys
	 */
	Set<String> getKeysByPrefix(String prefix);

	/**
	 * Check if a key exists.
	 *
	 * @param key the message key
	 * @return true if exists
	 */
	boolean exists(String key);

	/**
	 * Get all entries.
	 *
	 * @return map of key to entry
	 */
	Map<String, MessageEntry> getAllEntries();
}