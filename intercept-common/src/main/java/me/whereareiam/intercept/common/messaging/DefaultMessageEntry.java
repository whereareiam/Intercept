package me.whereareiam.intercept.common.messaging;

import lombok.Getter;
import me.whereareiam.intercept.messaging.MessageEntry;
import me.whereareiam.intercept.type.MessageType;

import java.util.Map;
import java.util.Set;

/**
 * Default implementation of MessageEntry.
 */
@Getter
public class DefaultMessageEntry implements MessageEntry {
	private final MessageType type;
	private final String singleText; // For single-language messages/templates
	private final Map<String, String> translations; // For multi-language messages

	public DefaultMessageEntry(MessageType type, String text) {
		this.type = type;
		this.singleText = text;
		this.translations = null;
	}

	public DefaultMessageEntry(MessageType type, Map<String, String> translations) {
		this.type = type;
		this.singleText = null;
		this.translations = translations;
	}

	@Override
	public String getText(String locale, String defaultLocale, String messageKey) {
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
	public String getText(String locale) {
		if (singleText != null) return singleText;
		if (translations == null) return null;

		return translations.get(locale);
	}

	@Override
	public String getText() {
		return singleText;
	}

	@Override
	public Set<String> getLocales() {
		return translations != null ? translations.keySet() : Set.of();
	}

	@Override
	public boolean hasTranslations() {
		return translations != null && !translations.isEmpty();
	}
}