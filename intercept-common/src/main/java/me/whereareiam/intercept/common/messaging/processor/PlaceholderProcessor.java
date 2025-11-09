package me.whereareiam.intercept.common.messaging.processor;

import me.whereareiam.intercept.common.util.MessageTags;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes placeholder tags in messages.
 * Replaces <p:name> with values from placeholder map.
 */
public class PlaceholderProcessor {
	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("<" + MessageTags.PLACEHOLDER_PREFIX + ":([a-zA-Z0-9_\\-]+)>");

	/**
	 * Process placeholders in text.
	 *
	 * @param text         the text containing placeholders
	 * @param placeholders the placeholder values
	 * @return text with placeholders replaced
	 */
	public String process(String text, Map<String, Object> placeholders) {
		if (text == null || text.isEmpty()) return text;

		if (!text.contains(MessageTags.PLACEHOLDER_TAG))
			return text; // Fast path: no placeholders

		Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
		StringBuilder result = new StringBuilder();

		while (matcher.find()) {
			String placeholderName = matcher.group(1);

			String replacement;
			if (!placeholders.containsKey(placeholderName)) {
				// Keep original placeholder if not found
				replacement = matcher.group(0);
			} else {
				Object value = placeholders.get(placeholderName);
				// Convert null to empty string
				replacement = Matcher.quoteReplacement(value != null ? String.valueOf(value) : "");
			}

			matcher.appendReplacement(result, replacement);
		}
		matcher.appendTail(result);

		return result.toString();
	}
}