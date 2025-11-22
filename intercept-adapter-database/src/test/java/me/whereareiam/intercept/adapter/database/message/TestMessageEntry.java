package me.whereareiam.intercept.adapter.database.message;

import me.whereareiam.intercept.messaging.MessageEntry;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;
import me.whereareiam.intercept.type.message.MessageType;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Test-only implementation of MessageEntry for integration tests.
 * Does not depend on intercept-common module.
 */
public class TestMessageEntry implements MessageEntry {
	private final MessageType type;
	private final String singleText;
	private final Map<Locale, String> translations;
	private final List<CompiledRegexPattern> regexPatterns;

	public TestMessageEntry(MessageType type, String text) {
		this(type, text, null);
	}

	public TestMessageEntry(MessageType type, String text, List<CompiledRegexPattern> regexPatterns) {
		this.type = type;
		this.singleText = text;
		this.translations = null;
		this.regexPatterns = regexPatterns != null ? regexPatterns : List.of();
	}

	public TestMessageEntry(MessageType type, Map<Locale, String> translations) {
		this(type, translations, null);
	}

	public TestMessageEntry(MessageType type, Map<Locale, String> translations, List<CompiledRegexPattern> regexPatterns) {
		this.type = type;
		this.singleText = null;
		this.translations = translations;
		this.regexPatterns = regexPatterns != null ? regexPatterns : List.of();
	}

	@Override
	public MessageType getType() {
		return type;
	}

	@Override
	public String getText(Locale locale, Locale defaultLocale, String messageKey) {
		if (singleText != null) return singleText;
		if (translations == null) return messageKey;

		// Try exact locale
		String text = translations.get(locale);
		if (text != null) return text;

		// Try default locale
		if (defaultLocale != null) {
			text = translations.get(defaultLocale);
			if (text != null) return text;
		}

		// Return message key as fallback
		return messageKey;
	}

	@Override
	public String getText(Locale locale) {
		if (singleText != null) return singleText;
		if (translations == null) return null;

		return translations.get(locale);
	}

	@Override
	public String getText() {
		return singleText;
	}

	@Override
	public Set<Locale> getLocales() {
		return translations != null ? translations.keySet() : Set.of();
	}

	@Override
	public boolean hasTranslations() {
		return translations != null && !translations.isEmpty();
	}

	@Override
	public List<CompiledRegexPattern> getRegexPatterns() {
		return regexPatterns;
	}

	@Override
	public boolean hasRegexPatterns() {
		return !regexPatterns.isEmpty();
	}
}