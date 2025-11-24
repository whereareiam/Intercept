package me.whereareiam.intercept.common.messaging.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.common.messaging.processor.TextProcessor;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.messaging.MessageRegistry;
import me.whereareiam.intercept.messaging.file.MessageFileLoader;
import me.whereareiam.intercept.model.messaging.CompiledMessageEntry;
import me.whereareiam.intercept.model.messaging.document.MessageDocument;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentEntry;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentRegex;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;
import me.whereareiam.intercept.type.message.MessageType;
import me.whereareiam.intercept.util.LocaleUtil;

import java.util.*;

/**
 * Default implementation that converts parsed persistence data into registry entries.
 */
@Singleton
public class DefaultMessageFileLoader implements MessageFileLoader {
	private final MessageRegistry registry;
	private final TextProcessor textProcessor;

	@Inject
	public DefaultMessageFileLoader(MessageRegistry registry, TextProcessor textProcessor) {
		this.registry = registry;
		this.textProcessor = textProcessor;
	}

	@Override
	public void loadFromData(String keyPrefix, MessageDocument fileData) {
		MessageType fileType = fileData.getType();

		for (Map.Entry<String, MessageDocumentEntry> entry : fileData.getItems().entrySet()) {
			String entryKey = entry.getKey();
			MessageDocumentEntry entryData = entry.getValue();

			String fullKey = keyPrefix.isEmpty() ? entryKey : keyPrefix + "." + entryKey;

			CompiledMessageEntry compiledMessageEntry = convertToMessageEntry(entryData, fileType);
			registry.register(fullKey, compiledMessageEntry);
		}
	}

	private CompiledMessageEntry convertToMessageEntry(MessageDocumentEntry entryData, MessageType fileType) {
		MessageType type = entryData.getType();
		if (type == null) type = fileType;
		if (type == null) type = autoDetectType(entryData);

		List<CompiledRegexPattern> regexPatterns = compileRegexPatterns(entryData.getRegex());

		if (entryData.getTranslations() != null && !entryData.getTranslations().isEmpty()) {
			Map<Locale, String> processedTranslations = new HashMap<>();
			for (Map.Entry<String, Object> trans : entryData.getTranslations().entrySet()) {
				Locale locale = parseLocale(trans.getKey());
				String text = textProcessor.process(trans.getValue());
				processedTranslations.put(locale, text);
			}
			return new CompiledMessageEntry(type, processedTranslations, regexPatterns);
		}

		if (entryData.getText() != null) {
			String text = textProcessor.process(entryData.getText());
			return new CompiledMessageEntry(type, text, regexPatterns);
		}

		throw new IllegalArgumentException("Message entry must have either 'text' or 'translations'");
	}

	private List<CompiledRegexPattern> compileRegexPatterns(List<MessageDocumentRegex> patterns) {
		if (patterns == null || patterns.isEmpty()) return List.of();

		List<CompiledRegexPattern> compiled = new ArrayList<>();
		for (MessageDocumentRegex patternData : patterns) {
			try {
				compiled.add(new CompiledRegexPattern(
						patternData.getPattern(),
						patternData.getPlaceholders(),
						patternData.getPriority(),
						patternData.isReplaceMatched()
				));
			} catch (Exception e) {
				Logger.severe("[Intercept] Failed to compile regex pattern: " + patternData.getPattern());
				e.printStackTrace();
			}
		}

		return compiled;
	}

	private MessageType autoDetectType(MessageDocumentEntry entryData) {
		if (entryData.getTranslations() != null && !entryData.getTranslations().isEmpty())
			return MessageType.MESSAGE;

		if (entryData.getText() != null)
			return MessageType.TEMPLATE;

		return MessageType.MESSAGE;
	}

	private Locale parseLocale(String localeString) {
		if (localeString == null || localeString.isEmpty())
			return Locale.getDefault();

		if ("default".equals(localeString))
			return new Locale.Builder().setLanguage("default").build();

		return LocaleUtil.parseLocale(localeString);
	}
}