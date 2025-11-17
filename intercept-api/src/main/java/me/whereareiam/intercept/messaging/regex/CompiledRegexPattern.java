package me.whereareiam.intercept.messaging.regex;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A compiled regex pattern with placeholder mappings.
 * Optimized for performance with literal prefix extraction and caching.
 */
@Getter
	public class CompiledRegexPattern {
	private final Pattern pattern;
	private final String regex;
	private final Map<String, String> placeholders;
	private final int priority;
	private final String literalPrefix;
	private final boolean replaceMatched;

	/**
	 * Create a compiled regex pattern.
	 *
	 * @param regex              the regex pattern string
	 * @param placeholders       map of placeholder names to capture group references (e.g., "$1", "$2")
	 * @param priority           priority for pattern matching (higher = checked first)
	 * @param replaceMatched whether only the matched substring should be replaced
	 */
	public CompiledRegexPattern(String regex, Map<String, String> placeholders, int priority, boolean replaceMatched) {
		this.regex = regex;
		this.pattern = Pattern.compile(regex);
		this.placeholders = placeholders != null ? placeholders : Map.of();
		this.priority = priority;
		this.literalPrefix = extractLiteralPrefix(regex);
		this.replaceMatched = replaceMatched;
	}

	/**
	 * Try to match text against this pattern and extract placeholders.
	 *
	 * @param text the text to match
	 * @return match result that includes placeholder values and match bounds, or empty if no match
	 */
	public Optional<MatchResult> match(String text) {
		// Fast path: check literal prefix first
		if (!hasLiteralPrefix(text)) return Optional.empty();

		Matcher matcher = pattern.matcher(text);
		if (!matcher.find()) return Optional.empty();

		Map<String, Object> result = new HashMap<>();
		for (Map.Entry<String, String> entry : placeholders.entrySet()) {
			String captureRef = entry.getValue(); // e.g., "$1", "$2"

			// Extract capture group number
			if (captureRef.startsWith("$")) {
				try {
					int groupNum = Integer.parseInt(captureRef.substring(1));
					if (groupNum <= matcher.groupCount()) {
						String value = matcher.group(groupNum);
						result.put(entry.getKey(), value);
					}
				} catch (NumberFormatException e) {
					// Invalid capture group reference, skip
				}
			}
		}

		return Optional.of(new MatchResult(result, matcher.start(), matcher.end(), matcher.group()));
	}

	/**
	 * Check if text has the literal prefix (fast check before full regex).
	 *
	 * @param text the text to check
	 * @return true if text contains the literal prefix, or true if no literal prefix
	 */
	public boolean hasLiteralPrefix(String text) {
		if (literalPrefix == null) return true;

		// If pattern is case-insensitive, do case-insensitive prefix check
		if ((pattern.flags() & Pattern.CASE_INSENSITIVE) != 0)
			return text.toLowerCase().contains(literalPrefix.toLowerCase());

		return text.contains(literalPrefix);
	}

	/**
	 * Extract literal prefix from regex pattern for optimization.
	 * Returns the longest literal string at the start of the pattern.
	 *
	 * @param regex the regex pattern
	 * @return the literal prefix, or null if pattern starts with special chars
	 */
	private String extractLiteralPrefix(String regex) {
		if (regex == null || regex.isEmpty()) return null;

		StringBuilder prefix = new StringBuilder();
		boolean escaped = false;
		int i = 0;

		// Skip anchors
		if (regex.startsWith("^"))
			i = 1;

		if (regex.startsWith("(?i)") || regex.startsWith("(?-i)"))
			i = 4;

		if (regex.startsWith("^(?i)") || regex.startsWith("^(?-i)"))
			i = 5;

		// Extract literal characters
		for (; i < regex.length(); i++) {
			char c = regex.charAt(i);

			if (escaped) {
				// Add escaped character as literal
				if (isLiteralEscape(c)) {
					prefix.append(c);
				} else {
					// Non-literal escape (e.g., \w, \d), stop here
					break;
				}
				escaped = false;
			} else if (c == '\\') {
				escaped = true;
			} else if (isRegexSpecialChar(c)) {
				// Hit a special regex character, stop here
				break;
			} else {
				prefix.append(c);
			}
		}

		String result = prefix.toString();
		return result.length() >= 3 ? result : null; // Only use if prefix is meaningful (3+ chars)
	}

	/**
	 * Check if character is a regex special character.
	 */
	private boolean isRegexSpecialChar(char c) {
		return c == '.' || c == '*' || c == '+' || c == '?' || c == '|' ||
				c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}' ||
				c == '$' || c == '^';
	}

	/**
	 * Check if escaped character represents a literal (not a special escape).
	 */
	private boolean isLiteralEscape(char c) {
		// These are escaped to be literal
		return c == '\\' || c == '.' || c == '*' || c == '+' || c == '?' || c == '|' ||
				c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}' ||
				c == '$' || c == '^' || c == ' ' || c == ':' || c == '\'' || c == '"';
	}

	/**
	 * Result of a successful regex match.
	 *
	 * @param placeholders map of placeholder names to extracted values
	 * @param start        start index (inclusive) of the match
	 * @param end          end index (exclusive) of the match
	 * @param matchedText  the exact substring that matched the pattern
	 */
	public record MatchResult(Map<String, Object> placeholders, int start, int end, String matchedText) {
	}
}