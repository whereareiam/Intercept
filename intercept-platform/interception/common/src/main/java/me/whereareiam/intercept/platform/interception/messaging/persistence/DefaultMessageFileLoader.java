package me.whereareiam.intercept.platform.interception.messaging.persistence;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.type.MultiValue;
import me.whereareiam.intercept.common.messaging.processor.TextProcessor;
import me.whereareiam.intercept.platform.interception.messaging.file.MessageFileLoader;
import me.whereareiam.intercept.platform.interception.messaging.MessageDocument;
import me.whereareiam.intercept.util.LocaleUtil;
import me.whereareiam.semantica.model.SemanticLocale;
import me.whereareiam.semantica.model.translation.entry.LocalizedEntry;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.base.TranslationLocale;

import java.util.*;

/**
 * Default implementation that converts parsed persistence data into registry entries.
 */
@Singleton
public class DefaultMessageFileLoader implements MessageFileLoader {
	private final TextProcessor textProcessor;
	private final Provider<Locale> defaultLocaleProvider;

	@Inject
	public DefaultMessageFileLoader(
			TextProcessor textProcessor,
			@Named("defaultLocale") Provider<Locale> defaultLocaleProvider
	) {
		this.textProcessor = textProcessor;
		this.defaultLocaleProvider = defaultLocaleProvider;
	}

	@Override
	public Map<String, TranslationEntry> loadFromData(String keyPrefix, MessageDocument fileData) {
		if (fileData == null || fileData.getEntries().isEmpty()) return Map.of();

		Map<String, ParsedEntry> parsedEntries = new LinkedHashMap<>();
		parseEntries(normalizePrefix(keyPrefix), fileData.getEntries(), parsedEntries);

		Map<String, TranslationEntry> translations = new HashMap<>();
		for (ParsedEntry entry : parsedEntries.values()) {
			TranslationEntry translationEntry = toTranslationEntry(entry);
			if (translationEntry != null)
				translations.put(entry.key(), translationEntry);
		}

		return translations.isEmpty() ? Map.of() : Map.copyOf(translations);
	}

	private String normalizePrefix(String keyPrefix) {
		if (keyPrefix == null || keyPrefix.isBlank()) return "";
		return keyPrefix.endsWith(".") ? keyPrefix.substring(0, keyPrefix.length() - 1) : keyPrefix;
	}

	private void parseEntries(String prefix, Map<String, MessageDocument.Node> source, Map<String, ParsedEntry> entries) {
		if (source == null || source.isEmpty()) return;

		for (Map.Entry<String, MessageDocument.Node> rawEntry : source.entrySet()) {
			String key = rawEntry.getKey();
			MessageDocument.Node value = rawEntry.getValue();
			if (value == null) continue;
			String escapedKey = escapeSegment(key);
			String fullKey;
			if (prefix == null || prefix.isEmpty()) {
				fullKey = escapedKey;
			} else if (prefix.endsWith(":")) {
				fullKey = prefix + escapedKey;
			} else {
				fullKey = prefix + "." + escapedKey;
			}

			if (value instanceof MessageDocument.Entry entryData) {
				ParsedEntry parsed = parseEntry(fullKey, entryData);
				entries.put(fullKey, parsed);
				continue;
			}

			if (value instanceof MessageDocument.Section section) {
				parseEntries(fullKey, section.getEntries(), entries);
			}
		}
	}

	private String escapeSegment(String segment) {
		if (segment == null || segment.isEmpty()) return segment;

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < segment.length(); i++) {
			char c = segment.charAt(i);
			if (c == '\\' || c == '.') builder.append('\\');
			builder.append(c);
		}

		return builder.toString();
	}

	private ParsedEntry parseEntry(String key, MessageDocument.Entry entry) {
		MultiValue<String> textRaw = entry.getText();
		Map<String, MultiValue<String>> localesRaw = entry.getLocales();
		boolean localized = localesRaw != null;
		if (textRaw != null && localized) {
			throw new IllegalArgumentException("Entry '" + key + "' cannot have both 'text' and 'locales'");
		}

		LocaleBlock locales = localized ? parseLocales(key, localesRaw) : LocaleBlock.empty();
		String text = localized ? null : parseText(textRaw);

		if (!localized && (text == null || text.isEmpty())) {
			throw new IllegalArgumentException("Entry '" + key + "' must define 'text'");
		}

		if (localized && locales.isEmpty()) {
			throw new IllegalArgumentException("Entry '" + key + "' must define at least one locale or default");
		}

		return new ParsedEntry(
				key,
				localized,
				text,
				locales.defaultText(),
				locales.translations()
		);
	}

	private String parseText(MultiValue<String> raw) {
		if (raw == null) return null;
		return textProcessor.process(raw);
	}

	private LocaleBlock parseLocales(String key, Map<String, MultiValue<String>> localesMap) {
		if (localesMap == null) {
			throw new IllegalArgumentException("Entry '" + key + "' locales must be a map");
		}

		String defaultText = null;
		Map<Locale, String> translations = new LinkedHashMap<>();

		for (Map.Entry<String, MultiValue<String>> localeEntry : localesMap.entrySet()) {
			String localeKey = localeEntry.getKey();
			String text = textProcessor.process(localeEntry.getValue());
			if ("default".equalsIgnoreCase(localeKey)) {
				defaultText = text;
				continue;
			}
			Locale locale = parseLocale(localeKey);
			translations.put(locale, text);
		}

		return new LocaleBlock(defaultText, translations);
	}

	private Locale parseLocale(String localeString) {
		if (localeString == null || localeString.isEmpty())
			return Locale.getDefault();

		if ("default".equalsIgnoreCase(localeString))
			return new Locale.Builder().setLanguage("default").build();

		return LocaleUtil.parseLocale(localeString);
	}

	private TranslationEntry toTranslationEntry(ParsedEntry entry) {
		if (entry == null) return null;

		if (entry.localized()) {
			Map<TranslationLocale, String> translations = new HashMap<>();
			for (Map.Entry<Locale, String> translation : entry.locales().entrySet()) {
				translations.put(SemanticLocale.wrap(translation.getKey()), translation.getValue());
			}

			String defaultText = entry.defaultText();
			if (defaultText != null) {
				Locale defaultLocale = defaultLocaleProvider == null ? Locale.US : defaultLocaleProvider.get();
				if (defaultLocale == null)
					defaultLocale = Locale.US;
				TranslationLocale defaultTranslationLocale = SemanticLocale.wrap(defaultLocale);
				translations.putIfAbsent(defaultTranslationLocale, defaultText);
			}

			if (!translations.isEmpty())
				return new LocalizedEntry(translations);

			return defaultText != null ? new TemplateEntry(defaultText) : null;
		}

		String text = entry.text();
		return text != null ? new TemplateEntry(text) : null;
	}

	private record LocaleBlock(String defaultText, Map<Locale, String> translations) {
		boolean isEmpty() {
			return (defaultText == null || defaultText.isEmpty()) && (translations == null || translations.isEmpty());
		}

		static LocaleBlock empty() {
			return new LocaleBlock(null, Map.of());
		}
	}

	private record ParsedEntry(
			String key,
			boolean localized,
			String text,
			String defaultText,
			Map<Locale, String> locales
	) {
	}
}
