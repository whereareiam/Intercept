package me.whereareiam.intercept.util;

import java.util.Locale;

/**
 * Utility class for locale-related operations.
 */
public final class LocaleUtil {
	/**
	 * Parses a locale string (e.g., "en_US") into a Locale object.
	 * Compatible with Java 17.
	 *
	 * @param localeString The locale string to parse
	 * @return The parsed Locale object
	 */
	public static Locale parseLocale(String localeString) {
		String[] parts = localeString.split("_", 2);
		return parts.length > 1
				? new Locale(parts[0], parts[1])
				: new Locale(parts[0]);
	}
}