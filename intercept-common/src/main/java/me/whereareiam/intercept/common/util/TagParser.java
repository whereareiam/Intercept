package me.whereareiam.intercept.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing custom tags from text.
 * Handles extraction and parsing of tags with configurable delimiters and attributes.
 * <p>
 * Performance optimizations:
 * - Caches parsed tag formats to avoid repeated parsing
 * - Caches compiled regex patterns to avoid expensive recompilation
 * - Thread-safe caching using ConcurrentHashMap
 */
public final class TagParser {
	// Performance: Cache parsed tag formats to avoid repeated string operations
	private static final Map<String, TagFormat> TAG_FORMAT_CACHE = new ConcurrentHashMap<>();

	// Performance: Cache compiled patterns to avoid expensive regex compilation
	private static final Map<String, Pattern> TAG_PATTERN_CACHE = new ConcurrentHashMap<>();

	// Performance: Cache attribute pattern (same for all tags)
	// Supports both quoted and unquoted attribute values:
	// - key="value" (group 1: key, group 2: quoted value)
	// - key=value (group 1: key, group 3: unquoted value)
	private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("(\\w+)=(?:\"([^\"]*)\"|([^\\s\"]+))");

	/**
	 * Check if a text contains a specific tag.
	 *
	 * @param text      the text to check
	 * @param tagFormat the full tag format to look for (e.g., "&lt;lang&gt;", "[l]", "{lang}")
	 * @return true if the text contains the tag
	 */
	public static boolean containsTag(String text, String tagFormat) {
		if (text == null || text.isEmpty()) return false;

		TagFormat parsed = getCachedTagFormat(tagFormat);
		if (parsed == null) return false;

		// Look for the opening delimiter followed by the tag name and a space (for attributes)
		return text.contains(parsed.openingDelimiter() + parsed.tagName() + " ");
	}

	/**
	 * Extract all tags from plain text.
	 * Parses tag syntax based on the configured format.
	 * Supports both quoted and unquoted attribute values.
	 * <p>
	 * Examples with quoted values:
	 * - {@code <lang key="value" param1="value1">} with format "{@code <lang>}"
	 * - {@code [l key="value" param1="value1"]} with format "{@code [l]}"
	 * - {@code {tr key="value" param1="value1"}} with format "{@code {tr}}"
	 * <p>
	 * Examples with unquoted values:
	 * - {@code <lang key=value param1=value1>} with format "{@code <lang>}"
	 * - {@code [l key=value param1=value1]} with format "{@code [l]}"
	 * - {@code {tr key=value param1=value1}} with format "{@code {tr}}"
	 *
	 * @param text      the plain text to search
	 * @param tagFormat the full tag format (e.g., "&lt;lang&gt;", "[l]", "{tr}")
	 * @return list of parsed tag data
	 */
	public static List<TagData> extractTags(String text, String tagFormat) {
		List<TagData> tags = new ArrayList<>();

		if (text == null || text.isEmpty()) return tags;

		TagFormat format = getCachedTagFormat(tagFormat);
		if (format == null) return tags;

		// Get cached pattern or compile and cache it
		Pattern pattern = getCachedPattern(tagFormat, format);
		Matcher matcher = pattern.matcher(text);

		while (matcher.find()) {
			String fullTag = matcher.group(0);
			String attributes = matcher.group(1);

			TagData tagData = parseAttributes(fullTag, attributes);
			if (tagData != null) tags.add(tagData);
		}

		return tags;
	}

	/**
	 * Get cached TagFormat or parse and cache it.
	 * This avoids repeated parsing of the same tag format string.
	 *
	 * @param tagFormat the full tag format string
	 * @return parsed TagFormat or null if invalid
	 */
	private static TagFormat getCachedTagFormat(String tagFormat) {
		if (tagFormat == null) return null;

		// Try to get from cache first
		return TAG_FORMAT_CACHE.computeIfAbsent(tagFormat, TagParser::parseTagFormat);
	}

	/**
	 * Get cached Pattern or compile and cache it.
	 * This avoids expensive regex compilation for the same tag format.
	 *
	 * @param tagFormat the tag format key for caching
	 * @param format    the parsed tag format
	 * @return compiled Pattern
	 */
	private static Pattern getCachedPattern(String tagFormat, TagFormat format) {
		return TAG_PATTERN_CACHE.computeIfAbsent(tagFormat, key -> {
			// Pattern to match: openingDelim + tagName + space + attributes + closingDelim
			String patternStr = Pattern.quote(format.openingDelimiter()) +
					Pattern.quote(format.tagName()) +
					"\\s+([^" + Pattern.quote(format.closingDelimiter()) + "]+)" +
					Pattern.quote(format.closingDelimiter());

			return Pattern.compile(patternStr);
		});
	}

	/**
	 * Parse a tag format string into its components.
	 * Expected format: openingDelimiter + tagName + closingDelimiter
	 * <p>
	 * Note: This method is called via cache, not directly.
	 * <p>
	 * Examples:
	 * - "{@code <lang>}" → opening: "&lt;", name: "lang", closing: "&gt;"
	 * - "{@code [l]}" → opening: "[", name: "l", closing: "]"
	 * - "{@code {tr}}" → opening: "{", name: "tr", closing: "}"
	 *
	 * @param tagFormat the full tag format string
	 * @return parsed TagFormat or null if invalid
	 */
	private static TagFormat parseTagFormat(String tagFormat) {
		if (tagFormat == null || tagFormat.length() < 3) return null;

		// Extract first character as opening delimiter
		String opening = String.valueOf(tagFormat.charAt(0));

		// Extract last character as closing delimiter
		String closing = String.valueOf(tagFormat.charAt(tagFormat.length() - 1));

		// Extract middle part as tag name
		String tagName = tagFormat.substring(1, tagFormat.length() - 1);

		return new TagFormat(opening, tagName, closing);
	}

	/**
	 * Parse attributes from a tag string.
	 * Supports both quoted and unquoted attribute values:
	 * - key="value" param1="value1" (quoted)
	 * - key=value param1=value1 (unquoted)
	 * - key="value" param1=value2 (mixed)
	 * <p>
	 * Performance: Uses cached pattern to avoid repeated compilation.
	 *
	 * @param fullTag    the complete tag text
	 * @param attributes the attribute string
	 * @return parsed TagData or null if key attribute is missing
	 */
	private static TagData parseAttributes(String fullTag, String attributes) {
		// Use cached pattern instead of compiling each time
		Matcher attrMatcher = ATTRIBUTE_PATTERN.matcher(attributes);

		String key = null;
		List<TagData.Placeholder> placeholders = new ArrayList<>();

		while (attrMatcher.find()) {
			String attrName = attrMatcher.group(1);
			// Group 2 is for quoted values, group 3 is for unquoted values
			String attrValue = attrMatcher.group(2) != null ? attrMatcher.group(2) : attrMatcher.group(3);

			if ("key".equals(attrName)) {
				key = attrValue;
				continue;
			}

			placeholders.add(new TagData.Placeholder(attrName, attrValue));
		}

		// key attribute is required
		if (key == null) return null;

		return new TagData(fullTag, key, placeholders);
	}

	/**
	 * Represents parsed tag format configuration.
	 * Contains the delimiters and tag name extracted from the format string.
	 */
	private record TagFormat(String openingDelimiter, String tagName, String closingDelimiter) {}

	/**
	 * Represents parsed tag data extracted from text.
	 */
	public record TagData(String originalTag, String key, List<Placeholder> placeholders) {
		/**
		 * Represents a single placeholder key-value pair.
		 */
		public record Placeholder(String name, String value) {}
	}
}


