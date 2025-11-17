package me.whereareiam.intercept.util;

/**
 * Utility class for working with regex patterns.
 */
public final class RegexHelper {
	/**
	 * Escapes a string to be used as a literal regex pattern.
	 * Escapes special regex characters individually so users can manually add placeholders.
	 * This is different from Pattern.quote() which wraps text in \Q...\E.
	 *
	 * @param text The text to escape
	 * @return The escaped regex pattern (without \Q...\E wrapper)
	 */
	public static String escapeRegex(String text) {
		if (text == null) return null;

		// Escape special regex characters individually
		// This allows users to manually add placeholders like (.*?) or (\w+) where needed
		// Newlines and other whitespace are preserved as-is
		StringBuilder escaped = new StringBuilder(text.length() * 2);
		for (char c : text.toCharArray()) {
			if (isSpecialRegexChar(c))
				escaped.append('\\');

			escaped.append(c);
		}

		return escaped.toString();
	}

	/**
	 * Check if a character is a special regex character that needs escaping.
	 * Includes backslash since it's used for escaping.
	 *
	 * @param c The character to check
	 * @return true if the character needs escaping
	 */
	public static boolean isSpecialRegexChar(char c) {
		return c == '.' || c == '*' || c == '+' || c == '?' || c == '|' ||
				c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}' ||
				c == '^' || c == '$' || c == '\\';
	}

	/**
	 * Check if a character is a special regex character (excluding backslash).
	 * Useful when backslashes are handled separately as escape characters.
	 *
	 * @param c The character to check
	 * @return true if the character is a special regex character (excluding backslash)
	 */
	public static boolean isSpecialRegexCharExcludingBackslash(char c) {
		return c == '.' || c == '*' || c == '+' || c == '?' || c == '|' ||
				c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}' ||
				c == '^' || c == '$';
	}

	/**
	 * Check if an escaped character represents a literal (not a special escape sequence).
	 * Used when parsing regex patterns to determine if an escaped character should be
	 * treated as a literal character.
	 *
	 * @param c The escaped character to check
	 * @return true if the escaped character represents a literal
	 */
	public static boolean isLiteralEscape(char c) {
		// These are escaped to be literal
		return c == '\\' || c == '.' || c == '*' || c == '+' || c == '?' || c == '|' ||
				c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}' ||
				c == '$' || c == '^' || c == ' ' || c == ':' || c == '\'' || c == '"';
	}
}

