package me.whereareiam.intercept.common.messaging.processor;

import me.whereareiam.intercept.common.util.MessageTags;
import me.whereareiam.intercept.messaging.MessageEntry;
import me.whereareiam.intercept.messaging.MessageRegistry;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes message reference tags.
 * Replaces <m:key> with the referenced message/template/palette.
 */
public class MessageReferenceProcessor {
	private static final Pattern MESSAGE_REF_PATTERN = Pattern.compile("<" + MessageTags.MESSAGE_REF_PREFIX + ":([a-zA-Z0-9_.\\-]+)>");
	private static final int MAX_DEPTH = 10;

	private final MessageRegistry registry;

	public MessageReferenceProcessor(MessageRegistry registry) {
		this.registry = registry;
	}

	/**
	 * Process message references in text.
	 *
	 * @param text   the text containing references
	 * @param locale the locale for multi-language entries
	 * @return text with references resolved
	 */
	public String process(String text, Locale locale) {
		if (text == null || text.isEmpty()) return text;

		if (!text.contains(MessageTags.MESSAGE_REF_TAG))
			return text; // Fast path: no references

		return resolveReferences(text, locale, new HashSet<>(), 0);
	}

	private String resolveReferences(String text, Locale locale, Set<String> resolutionPath, int depth) {
		if (depth >= MAX_DEPTH) return text; // Prevent infinite recursion

		Matcher matcher = MESSAGE_REF_PATTERN.matcher(text);
		StringBuilder result = new StringBuilder();

		while (matcher.find()) {
			String key = matcher.group(1);

			// Check for circular reference
			if (resolutionPath.contains(key)) {
				// Keep original to avoid infinite loop
				matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
				continue;
			}

			MessageEntry entry = registry.get(key);
			if (entry == null) {
				// Keep original if not found
				matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
				continue;
			}

			String entryText = entry.hasTranslations() ? entry.getText(locale) : entry.getText();
			if (entryText == null) {
				matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
				continue;
			}

			// Recursively resolve nested references
			Set<String> newPath = new HashSet<>(resolutionPath);
			newPath.add(key);
			String resolved = resolveReferences(entryText, locale, newPath, depth + 1);

			matcher.appendReplacement(result, Matcher.quoteReplacement(resolved));
		}
		matcher.appendTail(result);

		return result.toString();
	}
}