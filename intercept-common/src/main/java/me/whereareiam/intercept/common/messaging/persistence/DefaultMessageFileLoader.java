package me.whereareiam.intercept.common.messaging.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.common.messaging.processor.TextProcessor;
import me.whereareiam.intercept.messaging.file.MessageFileLoader;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.messaging.document.MessageDocument;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentEntry;
import me.whereareiam.intercept.util.LocaleUtil;
import me.whereareiam.semantica.model.SemanticLocale;
import me.whereareiam.semantica.model.translation.entry.LocalizedEntry;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.TranslationService;
import me.whereareiam.semantica.translation.base.TranslationLocale;

import com.google.inject.Provider;

import java.util.*;

/**
 * Default implementation that converts parsed persistence data into registry entries.
 */
@Singleton
public class DefaultMessageFileLoader implements MessageFileLoader {
	private static final String LOCALES_KEY = "locales";
	private static final String TEXT_KEY = "text";

	private final TextProcessor textProcessor;
	private final TranslationService<Locale> translationService;
	private final Provider<Settings> settingsProvider;

	@Inject
	public DefaultMessageFileLoader(
			TextProcessor textProcessor,
			TranslationService<Locale> translationService,
			Provider<Settings> settingsProvider
	) {
		this.textProcessor = textProcessor;
		this.translationService = translationService;
		this.settingsProvider = settingsProvider;
	}

	@Override
	public void loadFromData(String keyPrefix, MessageDocument fileData) {
		if (fileData == null || fileData.isEmpty()) return;

		Map<String, ParsedEntry> parsedEntries = new LinkedHashMap<>();
		parseEntries(normalizePrefix(keyPrefix), fileData.getEntries(), parsedEntries);

		Map<String, TranslationEntry> translations = new HashMap<>();
		for (ParsedEntry entry : parsedEntries.values()) {
			TranslationEntry translationEntry = toTranslationEntry(entry);
			if (translationEntry != null)
				translations.put(entry.key(), translationEntry);
		}

		if (!translations.isEmpty())
			translationService.register(translations);
	}

	private String normalizePrefix(String keyPrefix) {
		if (keyPrefix == null || keyPrefix.isBlank()) return "";
		return keyPrefix.endsWith(".") ? keyPrefix.substring(0, keyPrefix.length() - 1) : keyPrefix;
	}

	private void parseEntries(String prefix, Map<String, Object> source, Map<String, ParsedEntry> entries) {
		if (source == null || source.isEmpty()) return;

		for (Map.Entry<String, Object> rawEntry : source.entrySet()) {
			String key = rawEntry.getKey();
			Object value = rawEntry.getValue();
			String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

			if (value instanceof MessageDocumentEntry entryData) {
				ParsedEntry parsed = parseEntry(fullKey, entryData.getText(), entryData.getLocales());
				entries.put(fullKey, parsed);
				continue;
			}

			if (value instanceof Map<?, ?> mapValue) {
				Map<String, Object> map = castMap(mapValue);
				if (isEntryMap(map)) {
					ParsedEntry parsed = parseEntry(fullKey, map.get(TEXT_KEY), map.get(LOCALES_KEY));
					entries.put(fullKey, parsed);
					continue;
				}

				parseEntries(fullKey, map, entries);
				continue;
			}

			if (value != null) {
				ParsedEntry parsed = parseEntry(fullKey, value, null);
				entries.put(fullKey, parsed);
			}
		}
	}

	private boolean isEntryMap(Map<String, Object> map) {
		return map.containsKey(TEXT_KEY) || map.containsKey(LOCALES_KEY);
	}

	private ParsedEntry parseEntry(String key, Object textRaw, Object localesRaw) {
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

	private String parseText(Object raw) {
		if (raw == null) return null;
		return textProcessor.process(raw);
	}

	private LocaleBlock parseLocales(String key, Object raw) {
		if (!(raw instanceof Map<?, ?> mapValue)) {
			throw new IllegalArgumentException("Entry '" + key + "' locales must be a map");
		}

		Map<String, Object> localesMap = castMap(mapValue);
		String defaultText = null;
		Map<Locale, String> translations = new LinkedHashMap<>();

		for (Map.Entry<String, Object> localeEntry : localesMap.entrySet()) {
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
				Locale defaultLocale = settingsProvider.get().getLocale();
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

	private Map<String, Object> castMap(Map<?, ?> raw) {
		Map<String, Object> casted = new LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : raw.entrySet()) {
			casted.put(String.valueOf(entry.getKey()), entry.getValue());
		}
		return casted;
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
