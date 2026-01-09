package me.whereareiam.intercept.adapter.database.message;

import me.whereareiam.intercept.Constants;

import java.util.Set;

/**
 * Utility class for resolving message keys and prefixes.
 */
public final class MessageKeyResolver {
	/**
	 * Find the key prefix that matches the full key.
	 * Returns the longest matching prefix.
	 *
	 * @param fullKey  the full message key (e.g., "errors.permissions.no-permission")
	 * @param prefixes set of available key prefixes (e.g., {"errors", "errors.permissions"})
	 * @return the longest matching prefix, or empty string if none found
	 */
	public static String findKeyPrefix(String fullKey, Set<String> prefixes) {
		String bestPrefix = "";
		for (String prefix : prefixes)
			if (fullKey.startsWith(prefix) && prefix.length() > bestPrefix.length())
				bestPrefix = prefix;

		return bestPrefix;
	}

	/**
	 * Extract entry key from full key given the key prefix.
	 *
	 * @param fullKey   the full message key (e.g., "errors.permissions.no-permission")
	 * @param keyPrefix the key prefix (e.g., "errors.permissions")
	 * @return the entry key (e.g., "no-permission")
	 */
	public static String extractEntryKey(String fullKey, String keyPrefix) {
		if (keyPrefix == null || keyPrefix.isEmpty()) return fullKey;
		if (fullKey.startsWith(keyPrefix)) {
			int start = keyPrefix.length();
			if (!keyPrefix.endsWith(Constants.Namespace.NAMESPACE_SEPARATOR) && fullKey.length() > start && fullKey.charAt(start) == '.')
				start++;

			return fullKey.substring(start);
		}

		return fullKey;
	}
}

