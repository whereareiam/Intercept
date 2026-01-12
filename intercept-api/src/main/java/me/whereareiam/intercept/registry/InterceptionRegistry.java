package me.whereareiam.intercept.registry;

import me.whereareiam.intercept.model.regex.CompiledRegexPattern;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Registry for interception rules (regex patterns) keyed by translation key.
 */
public interface InterceptionRegistry {
	/**
	 * Register interception patterns for a key, replacing any existing patterns.
	 *
	 * @param key      translation key
	 * @param patterns compiled regex patterns
	 */
	void register(String key, List<CompiledRegexPattern> patterns);

	/**
	 * Get compiled patterns for a key.
	 *
	 * @param key translation key
	 * @return list of patterns, empty if missing
	 */
	List<CompiledRegexPattern> get(String key);

	/**
	 * Get all keys with interception patterns.
	 *
	 * @return set of keys
	 */
	Set<String> getKeys();

	/**
	 * Get all registered patterns.
	 *
	 * @return map of key to patterns
	 */
	Map<String, List<CompiledRegexPattern>> getAll();

	/**
	 * Clear the registry.
	 */
	void clear();

	/**
	 * Clear all patterns for a specific namespace.
	 *
	 * @param namespace the namespace prefix to clear
	 */
	void clearNamespace(String namespace);
}
