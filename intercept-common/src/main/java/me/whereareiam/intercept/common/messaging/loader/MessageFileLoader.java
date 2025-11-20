package me.whereareiam.intercept.common.messaging.loader;

import me.whereareiam.intercept.common.messaging.DefaultMessageEntry;
import me.whereareiam.intercept.common.messaging.DefaultMessageRegistry;
import me.whereareiam.intercept.common.messaging.processor.TextProcessor;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;
import me.whereareiam.intercept.type.message.MessageType;
import me.whereareiam.intercept.util.LocaleUtil;

import java.util.*;

/**
 * Loads message files into the MessageRegistry.
 * Converts file data into MessageEntry instances and registers them.
 */
public class MessageFileLoader {
	private final TextProcessor textProcessor;
	private final DefaultMessageRegistry registry;

	public MessageFileLoader(TextProcessor textProcessor, DefaultMessageRegistry registry) {
		this.textProcessor = textProcessor;
		this.registry = registry;
	}

	/**
	 * Load messages from parsed file data into the registry.
	 *
	 * @param keyPrefix the key prefix for this file (e.g., "errors.permissions")
	 * @param fileData  the parsed file data
	 */
	public void loadFromData(String keyPrefix, MessageFileData fileData) {
		MessageType fileType = fileData.getType();

		for (Map.Entry<String, MessageEntryData> entry : fileData.getItems().entrySet()) {
			String entryKey = entry.getKey();
			MessageEntryData entryData = entry.getValue();

			// Build full key
			String fullKey = keyPrefix.isEmpty() ? entryKey : keyPrefix + "." + entryKey;

			// Convert to MessageEntry and register
			DefaultMessageEntry messageEntry = convertToMessageEntry(entryData, fileType);
			registry.register(fullKey, messageEntry);
		}
	}

	private DefaultMessageEntry convertToMessageEntry(MessageEntryData entryData, MessageType fileType) {
		// Determine type: entry type > file type > auto-detect
		MessageType type = entryData.getType();
		if (type == null) {
			type = fileType;
		}
		if (type == null) {
			type = autoDetectType(entryData);
		}

		// Compile regex patterns if present
		List<CompiledRegexPattern> regexPatterns = compileRegexPatterns(entryData.getRegex());

		// Convert text/translations
		if (entryData.getTranslations() != null && !entryData.getTranslations().isEmpty()) {
			// Multi-language message
			Map<Locale, String> processedTranslations = new HashMap<>();
			for (Map.Entry<String, Object> trans : entryData.getTranslations().entrySet()) {
				String localeString = trans.getKey();
				Locale locale = parseLocale(localeString);
				String text = textProcessor.process(trans.getValue());
				processedTranslations.put(locale, text);
			}
			return new DefaultMessageEntry(type, processedTranslations, regexPatterns);
		} else if (entryData.getText() != null) {
			// Single-language message or template
			String text = textProcessor.process(entryData.getText());
			return new DefaultMessageEntry(type, text, regexPatterns);
		} else {
			throw new IllegalArgumentException("Message entry must have either 'text' or 'translations'");
		}
	}

	/**
	 * Compile regex patterns from pattern data.
	 *
	 * @param patterns the regex pattern data list
	 * @return list of compiled patterns, or empty list if none
	 */
	private List<CompiledRegexPattern> compileRegexPatterns(List<RegexPatternData> patterns) {
		if (patterns == null || patterns.isEmpty()) {
			return List.of();
		}

		List<CompiledRegexPattern> compiled = new ArrayList<>();
		for (RegexPatternData patternData : patterns) {
			try {
				CompiledRegexPattern compiledPattern = new CompiledRegexPattern(
						patternData.getPattern(),
						patternData.getPlaceholders(),
						patternData.getPriority(),
						patternData.isReplaceMatched()
				);
				compiled.add(compiledPattern);
			} catch (Exception e) {
				// Log error but continue with other patterns
				Logger.severe("[Intercept] Failed to compile regex pattern: " + patternData.getPattern());
				e.printStackTrace();
			}
		}

		return compiled;
	}

	private MessageType autoDetectType(MessageEntryData entryData) {
		// If has translations, it's a message
		if (entryData.getTranslations() != null && !entryData.getTranslations().isEmpty())
			return MessageType.MESSAGE;

		// If only has text, it's a template
		if (entryData.getText() != null)
			return MessageType.TEMPLATE;

		// Default to message
		return MessageType.MESSAGE;
	}

	/**
	 * Parse a locale string from file format to Locale object.
	 * Handles formats like "en_US", "de_DE", "en", "default", etc.
	 *
	 * @param localeString the locale string from file (e.g., "en_US" or "default")
	 * @return Locale object, or special Locale for "default" case
	 */
	private Locale parseLocale(String localeString) {
		if (localeString == null || localeString.isEmpty())
			return Locale.getDefault();

		// Handle special "default" case
		if ("default".equals(localeString))
			return new Locale.Builder().setLanguage("default").build();

		return LocaleUtil.parseLocale(localeString);
	}
}