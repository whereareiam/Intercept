package me.whereareiam.intercept.util;

import java.util.Locale;

/**
 * Utility class for locale-related operations.
 */
public final class LocaleUtil {
	private LocaleUtil() {
	}

	/**
	 * Parses a locale string (e.g., "en") into a Locale object.
	 * Compatible with Java 17.
	 *
	 * @param localeString The locale string to parse
	 * @return The parsed Locale object
	 */
	public static Locale parseLocale(String localeString) {
		String[] parts = localeString.split("_", 3);
		return switch (parts.length) {
			case 1 -> new Locale(parts[0]);
			case 2 -> new Locale(parts[0], parts[1]);
			default -> new Locale(parts[0], parts[1], parts[2]);
		};
	}

	/**
	 * Formats a Locale into the underscore separated form expected by message files (e.g., "en").
	 *
	 * @param locale The locale to format
	 * @return The underscore separated locale string
	 */
	public static String formatLocale(Locale locale) {
		if (locale == null) return "";

		StringBuilder builder = new StringBuilder(locale.getLanguage());
		if (!locale.getCountry().isEmpty()) builder.append('_').append(locale.getCountry());
		if (!locale.getVariant().isEmpty()) builder.append('_').append(locale.getVariant());
		return builder.toString();
	}
}