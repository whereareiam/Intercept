package me.whereareiam.intercept.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for working with Adventure Components.
 * Provides methods to extract text and perform replacements while preserving formatting.
 * <p>
 * Performance optimizations:
 * - Caches parsed tag formats to avoid repeated parsing
 * - Caches compiled regex patterns to avoid expensive recompilation
 * - Thread-safe caching using ConcurrentHashMap
 */
public final class ComponentHelper {
	private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

	// Performance: Cache parsed tag formats to avoid repeated string operations
	private static final Map<String, TagFormat> TAG_FORMAT_CACHE = new ConcurrentHashMap<>();

	// Performance: Cache compiled patterns to avoid expensive regex compilation
	private static final Map<String, Pattern> TAG_PATTERN_CACHE = new ConcurrentHashMap<>();

	// Performance: Cache attribute pattern (same for all tags)
	private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("(\\w+)=\"([^\"]*)\"");

	/**
	 * Extract plain text from a component, removing all formatting.
	 * Used for pattern matching and tag detection.
	 *
	 * @param component the component to extract text from
	 * @return the plain text content
	 */
	public static String extractPlainText(Component component) {
		return PLAIN_SERIALIZER.serialize(component);
	}

	/**
	 * Check if a component's text contains a specific tag.
	 *
	 * @param component the component to check
	 * @param tagFormat the full tag format to look for (e.g., "&lt;lang&gt;", "[l]", "{lang}")
	 * @return true if the component contains the tag
	 */
	public static boolean containsTag(Component component, String tagFormat) {
		TagFormat parsed = getCachedTagFormat(tagFormat);
		if (parsed == null) return false;

		String plainText = extractPlainText(component);
		// Look for the opening delimiter followed by the tag name and a space (for attributes)
		return plainText.contains(parsed.getOpeningDelimiter() + parsed.getTagName() + " ");
	}

	/**
	 * Replace the entire text content of a component with new text.
	 * This creates a new text component, discarding original formatting.
	 * Use this when you want to completely replace the message.
	 *
	 * @param newText the new text to use
	 * @return a new text component with the new text
	 */
	public static Component replaceEntireText(String newText) {
		return Component.text(newText);
	}

	/**
	 * Extract all tags from plain text.
	 * Parses tag syntax based on the configured format.
	 * <p>
	 * Examples:
	 * - {@code <lang key="value" param1="value1">} with format "{@code <lang>}"
	 * - {@code [l key="value" param1="value1"]} with format "{@code [l]}"
	 * - {@code {tr key="value" param1="value1"}} with format "{@code {tr}}"
	 *
	 * @param text      the plain text to search
	 * @param tagFormat the full tag format (e.g., "&lt;lang&gt;", "[l]", "{tr}")
	 * @return list of parsed tag data
	 */
	public static List<TagData> extractTags(String text, String tagFormat) {
		List<TagData> tags = new ArrayList<>();

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
		return TAG_FORMAT_CACHE.computeIfAbsent(tagFormat, ComponentHelper::parseTagFormat);
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
			String patternStr = Pattern.quote(format.getOpeningDelimiter()) +
					Pattern.quote(format.getTagName()) +
					"\\s+([^" + Pattern.quote(format.getClosingDelimiter()) + "]+)" +
					Pattern.quote(format.getClosingDelimiter());

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
	 * Expected format: key="value" param1="value1" param2="value2"
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
			String attrValue = attrMatcher.group(2);

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
	 * Replace text in a component while preserving all formatting.
	 * Recursively traverses the component tree and performs replacements.
	 *
	 * @param component    the component to process
	 * @param replacements map of old text -> new text
	 * @return new component with replacements applied
	 */
	public static Component replaceTextInComponent(Component component, Map<String, String> replacements) {
		if (replacements.isEmpty()) return component;

		return traverseAndReplace(component, replacements);
	}

	/**
	 * Recursively traverse and replace text in component tree.
	 * Preserves all styling, colors, events, and child components.
	 */
	private static Component traverseAndReplace(Component component, Map<String, String> replacements) {
		// Handle text content if this is a TextComponent
		if (component instanceof TextComponent textComponent) {
			String content = textComponent.content();

			// Apply all replacements to the content
			for (Map.Entry<String, String> entry : replacements.entrySet())
				content = content.replace(entry.getKey(), entry.getValue());

			// Build new component with replaced content and same style
			TextComponent.Builder builder = Component.text()
					.content(content)
					.style(component.style());

			// Recursively process children
			for (Component child : component.children())
				builder.append(traverseAndReplace(child, replacements));

			return builder.build();
		}

		// For non-text components, just process children
		List<Component> children = component.children();
		if (!children.isEmpty()) {
			Component result = component;
			for (Component child : children)
				result = result.append(traverseAndReplace(child, replacements));

			return result;
		}

		// No changes needed for this component
		return component;
	}

	/**
	 * Represents parsed tag format configuration.
	 * Contains the delimiters and tag name extracted from the format string.
	 */
	@Getter
	@AllArgsConstructor
	private static class TagFormat {
		private final String openingDelimiter;
		private final String tagName;
		private final String closingDelimiter;
	}

	/**
	 * Represents parsed tag data extracted from text.
	 */
	@Getter
	@AllArgsConstructor
	public static class TagData {
		private final String originalTag;
		private final String key;
		private final List<Placeholder> placeholders;

		/**
		 * Represents a single placeholder key-value pair.
		 */
		@Getter
		@AllArgsConstructor
		public static class Placeholder {
			private final String name;
			private final String value;
		}
	}
}