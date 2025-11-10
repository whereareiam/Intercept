package me.whereareiam.intercept.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.format.Style;
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
	 * Replace text with Component values.
	 * When text matches, replaces it with the new Component preserving structure.
	 *
	 * @param component    the component to process
	 * @param replacements map of old text -> new Component
	 * @return new component with replacements applied
	 */
	public static Component replaceTextWithComponents(Component component, Map<String, Component> replacements) {
		if (replacements.isEmpty()) return component;

		return traverseAndReplaceWithComponents(component, replacements);
	}

	/**
	 * Recursively traverse and replace text nodes with Components.
	 */
	private static Component traverseAndReplaceWithComponents(Component component, Map<String, Component> replacements) {
		if (component instanceof TextComponent textComponent)
			return processTextComponent(textComponent, replacements);

		if (component instanceof TranslatableComponent translatable)
			return processTranslatableComponent(translatable, replacements);

		return processGenericComponent(component, replacements);
	}

	/**
	 * Process a TextComponent for replacements.
	 */
	private static Component processTextComponent(TextComponent textComponent, Map<String, Component> replacements) {
		String content = textComponent.content();

		// Exact match - replace entire component
		if (replacements.containsKey(content))
			return replaceExactMatch(textComponent, replacements.get(content), replacements);

		// Partial match - find the LEFTMOST match to process in order
		String earliestTarget = null;
		Component earliestReplacement = null;
		int earliestIndex = -1;

		for (Map.Entry<String, Component> entry : replacements.entrySet()) {
			int index = content.indexOf(entry.getKey());
			if (index != -1 && (earliestIndex == -1 || index < earliestIndex)) {
				earliestIndex = index;
				earliestTarget = entry.getKey();
				earliestReplacement = entry.getValue();
			}
		}

		// Found a match - split and replace (recursion handles subsequent matches)
		if (earliestTarget != null)
			return splitAndReplaceWithComponents(textComponent, earliestTarget, earliestReplacement, replacements);

		// No match - rebuild with processed children
		return rebuildWithChildren(textComponent, replacements);
	}

	/**
	 * Replace exact match and preserve style.
	 */
	private static Component replaceExactMatch(TextComponent original, Component replacement, Map<String, Component> replacements) {
		Component styled = replacement.style(original.style());

		for (Component child : original.children())
			styled = styled.append(traverseAndReplaceWithComponents(child, replacements));

		return styled;
	}

	/**
	 * Rebuild component with processed children (no content changes).
	 */
	private static Component rebuildWithChildren(TextComponent textComponent, Map<String, Component> replacements) {
		TextComponent.Builder builder = Component.text()
				.content(textComponent.content())
				.style(textComponent.style());

		for (Component child : textComponent.children())
			builder.append(traverseAndReplaceWithComponents(child, replacements));

		return builder.build();
	}

	/**
	 * Process a TranslatableComponent for replacements.
	 */
	private static Component processTranslatableComponent(TranslatableComponent translatable, Map<String, Component> replacements) {
		List<ComponentLike> processedArgs = processTranslationArguments(translatable.arguments(), replacements);

		TranslatableComponent.Builder builder = Component.translatable()
				.key(translatable.key())
				.style(translatable.style());

		if (!processedArgs.isEmpty())
			builder.arguments(processedArgs);

		for (Component child : translatable.children())
			builder.append(traverseAndReplaceWithComponents(child, replacements));

		return builder.build();
	}

	/**
	 * Process translation arguments recursively.
	 */
	private static List<ComponentLike> processTranslationArguments(List<TranslationArgument> arguments, Map<String, Component> replacements) {
		List<ComponentLike> processed = new ArrayList<>();

		for (TranslationArgument arg : arguments) {
			Object value = arg.value();

			if (value instanceof Component argComponent) {
				processed.add(traverseAndReplaceWithComponents(argComponent, replacements));
				continue;
			}

			if (value instanceof ComponentLike)
				processed.add((ComponentLike) value);
		}

		return processed;
	}

	/**
	 * Process generic components by traversing their children.
	 */
	private static Component processGenericComponent(Component component, Map<String, Component> replacements) {
		List<Component> children = component.children();
		if (children.isEmpty())
			return component;

		Component result = component;
		for (Component child : children)
			result = result.append(traverseAndReplaceWithComponents(child, replacements));

		return result;
	}

	/**
	 * Split text at target position and insert replacement Component.
	 */
	private static Component splitAndReplaceWithComponents(
			TextComponent original, String target, Component replacement, Map<String, Component> allReplacements
	) {
		String content = original.content();
		int index = content.indexOf(target);
		if (index == -1) return original;

		Component result = Component.empty().style(original.style());

		// Add prefix text (before match)
		if (index > 0) result = result.append(createStyledText(content.substring(0, index), original.style()));

		// Add replacement component
		result = result.append(replacement);

		// Add suffix text (after match) - recursively check for more replacements
		if (index + target.length() < content.length()) {
			String after = content.substring(index + target.length());
			Component afterComponent = createStyledText(after, original.style());
			result = result.append(traverseAndReplaceWithComponents(afterComponent, allReplacements));
		}

		// Append processed children
		for (Component child : original.children())
			result = result.append(traverseAndReplaceWithComponents(child, allReplacements));

		return result;
	}

	/**
	 * Create a text component with the given style.
	 */
	private static Component createStyledText(String text, Style style) {
		return Component.text(text).style(style);
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