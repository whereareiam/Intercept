package me.whereareiam.intercept.registry;

import me.whereareiam.intercept.persistence.format.ReservedKeyHandler;

import java.util.Optional;
import java.util.Set;

/**
 * Registry for reserved key handlers.
 */
@SuppressWarnings("unused")
public interface ReservedKeyRegistry {
	/**
	 * Register a handler.
	 *
	 * @param handler handler to register
	 */
	void register(ReservedKeyHandler handler);

	/**
	 * Unregister a handler.
	 *
	 * @param key reserved key
	 */
	void unregister(String key);

	/**
	 * Get a handler for a key.
	 *
	 * @param key reserved key
	 * @return handler if present
	 */
	Optional<ReservedKeyHandler> get(String key);

	/**
	 * Get all reserved keys (core + registered).
	 *
	 * @return reserved key set
	 */
	Set<String> getAllReservedKeys();

	/**
	 * Check whether a key is reserved.
	 *
	 * @param key key to check
	 * @return true if reserved
	 */
	boolean isReservedKey(String key);
}
