package me.whereareiam.intercept.model.messaging;

import lombok.Getter;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;
import me.whereareiam.intercept.type.message.MessageType;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Getter
public class CompiledMessageEntry {
	private final MessageType type;
	private final String singleText; // For single-language messages/templates
	private final Map<Locale, String> translations; // For multi-language messages
	private final List<CompiledRegexPattern> regexPatterns; // Regex patterns for this message

	public CompiledMessageEntry(MessageType type, String text) {
		this(type, text, null);
	}

	public CompiledMessageEntry(MessageType type, String text, List<CompiledRegexPattern> regexPatterns) {
		this.type = type;
		this.singleText = text;
		this.translations = null;
		this.regexPatterns = regexPatterns != null ? regexPatterns : List.of();
	}

	public CompiledMessageEntry(MessageType type, Map<Locale, String> translations) {
		this(type, translations, null);
	}

	public CompiledMessageEntry(MessageType type, Map<Locale, String> translations, List<CompiledRegexPattern> regexPatterns) {
		this.type = type;
		this.singleText = null;
		this.translations = translations;
		this.regexPatterns = regexPatterns != null ? regexPatterns : List.of();
	}

	public String getText(Locale locale, Locale defaultLocale, String messageKey) {
		if (singleText != null) return singleText;
		if (translations == null) return messageKey;

		// Try exact locale
		String text = translations.get(locale);
		if (text != null) return text;

		// Try to find nearest locale with same language code (e.g., en_US -> en_GB, en_CA, en, etc.)
		if (locale != null) {
			String languageCode = locale.getLanguage();
			if (!languageCode.isEmpty()) {
				for (Locale availableLocale : translations.keySet()) {
					if (availableLocale != null && languageCode.equals(availableLocale.getLanguage())) {
						text = translations.get(availableLocale);
						if (text != null) return text;
					}
				}
			}
		}

		// Try default locale
		if (defaultLocale != null) {
			text = translations.get(defaultLocale);
			if (text != null) return text;
		}

		// Try to find a locale with language "default" as fallback (special case)
		// This handles the case where files use "default" as a locale string
		for (Locale availableLocale : translations.keySet()) {
			if (availableLocale != null && "default".equals(availableLocale.getLanguage())) {
				text = translations.get(availableLocale);
				if (text != null) return text;
			}
		}

		// Return message key as fallback
		return messageKey;
	}

	public String getText(Locale locale) {
		if (singleText != null) return singleText;
		if (translations == null) return null;

		return translations.get(locale);
	}

	public String getText() {
		return singleText;
	}

	public Set<Locale> getLocales() {
		return translations != null ? translations.keySet() : Set.of();
	}

	public boolean hasTranslations() {
		return translations != null && !translations.isEmpty();
	}

	public boolean hasRegexPatterns() {
		return !regexPatterns.isEmpty();
	}
}