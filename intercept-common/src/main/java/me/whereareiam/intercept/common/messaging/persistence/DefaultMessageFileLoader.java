package me.whereareiam.intercept.common.messaging.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.common.messaging.processor.TextProcessor;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.messaging.InterceptionRegistry;
import me.whereareiam.intercept.messaging.MessageRegistry;
import me.whereareiam.intercept.messaging.file.MessageFileLoader;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.messaging.CompiledMessageEntry;
import me.whereareiam.intercept.model.messaging.document.MessageDocument;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentEntry;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentInterception;
import me.whereareiam.intercept.model.messaging.document.MessageDocumentRegex;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;
import me.whereareiam.intercept.type.message.MessageType;
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
	private static final String INTERCEPTION_KEY = "interception";
	private static final String PATTERNS_KEY = "patterns";

	private final MessageRegistry registry;
	private final InterceptionRegistry interceptionRegistry;
	private final TextProcessor textProcessor;
	private final TranslationService<Locale> translationService;
	private final Provider<Settings> settingsProvider;

	@Inject
	public DefaultMessageFileLoader(
			MessageRegistry registry,
			InterceptionRegistry interceptionRegistry,
			TextProcessor textProcessor,
			TranslationService<Locale> translationService,
			Provider<Settings> settingsProvider
	) {
		this.registry = registry;
		this.interceptionRegistry = interceptionRegistry;
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
			CompiledMessageEntry compiled = toCompiledEntry(entry);
			registry.register(entry.key(), compiled);
			if (!entry.regexPatterns().isEmpty()) {
				interceptionRegistry.register(entry.key(), entry.regexPatterns());
			}

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
				ParsedEntry parsed = parseEntry(fullKey, entryData.getText(), entryData.getLocales(), entryData.getInterception());
				if (parsed != null) entries.put(fullKey, parsed);
				continue;
			}

			if (value instanceof Map<?, ?> mapValue) {
				Map<String, Object> map = castMap(mapValue);
				if (isEntryMap(map)) {
					ParsedEntry parsed = parseEntry(fullKey, map.get(TEXT_KEY), map.get(LOCALES_KEY), map.get(INTERCEPTION_KEY));
					if (parsed != null) entries.put(fullKey, parsed);
				} else {
					parseEntries(fullKey, map, entries);
				}
				continue;
			}

			if (value != null) {
				ParsedEntry parsed = parseEntry(fullKey, value, null, null);
				if (parsed != null) entries.put(fullKey, parsed);
			}
		}
	}

	private boolean isEntryMap(Map<String, Object> map) {
		return map.containsKey(TEXT_KEY) || map.containsKey(LOCALES_KEY) || map.containsKey(INTERCEPTION_KEY);
	}

	private ParsedEntry parseEntry(String key, Object textRaw, Object localesRaw, Object interceptionRaw) {
		boolean localized = localesRaw != null;
		if (textRaw != null && localized) {
			throw new IllegalArgumentException("Entry '" + key + "' cannot have both 'text' and 'locales'");
		}

		LocaleBlock locales = localized ? parseLocales(key, localesRaw) : LocaleBlock.empty();
		String text = localized ? null : parseText(textRaw);
		List<CompiledRegexPattern> regexPatterns = parseInterception(key, interceptionRaw);

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
				locales.translations(),
				regexPatterns
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

	private List<CompiledRegexPattern> parseInterception(String key, Object raw) {
		if (raw == null) return List.of();

		if (raw instanceof MessageDocumentInterception interception) {
			return compileRegexPatterns(interception.getPatterns());
		}

		if (raw instanceof Map<?, ?> mapValue) {
			Map<String, Object> interception = castMap(mapValue);
			Object patternsRaw = interception.get(PATTERNS_KEY);
			return compileRegexPatterns(patternsRaw);
		}

		Logger.warn("[Intercept] Ignoring invalid interception section for key '%s'", key);
		return List.of();
	}

	private List<CompiledRegexPattern> compileRegexPatterns(Object patternsRaw) {
		if (patternsRaw == null) return List.of();

		if (patternsRaw instanceof List<?> list) {
			List<CompiledRegexPattern> compiled = new ArrayList<>();
			for (Object item : list) {
				MessageDocumentRegex regex = toDocumentRegex(item);
				if (regex == null) continue;
				addCompiledRegex(compiled, regex);
			}
			return compiled;
		}

		if (patternsRaw instanceof MessageDocumentRegex regex) {
			List<CompiledRegexPattern> compiled = new ArrayList<>();
			addCompiledRegex(compiled, regex);
			return compiled;
		}

		Logger.warn("[Intercept] Invalid interception patterns format: %s", patternsRaw.getClass().getSimpleName());
		return List.of();
	}

	private MessageDocumentRegex toDocumentRegex(Object raw) {
		if (raw instanceof MessageDocumentRegex regex) return regex;
		if (!(raw instanceof Map<?, ?> mapValue)) return null;

		Map<String, Object> map = castMap(mapValue);
		Object patternRaw = map.get("pattern");
		if (!(patternRaw instanceof String pattern) || pattern.isEmpty()) return null;

		MessageDocumentRegex regex = new MessageDocumentRegex();
		regex.setPattern(pattern);

		Object priorityRaw = map.get("priority");
		if (priorityRaw instanceof Number number) {
			regex.setPriority(number.intValue());
		}

		Object replaceMatchedRaw = map.get("replaceMatched");
		if (replaceMatchedRaw instanceof Boolean flag) {
			regex.setReplaceMatched(flag);
		}

		Object placeholdersRaw = map.get("placeholders");
		if (placeholdersRaw instanceof Map<?, ?> placeholdersMap) {
			Map<String, String> placeholders = new LinkedHashMap<>();
			for (Map.Entry<?, ?> entry : placeholdersMap.entrySet()) {
				String key = String.valueOf(entry.getKey());
				String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : null;
				if (value != null) placeholders.put(key, value);
			}
			regex.setPlaceholders(placeholders);
		}

		return regex;
	}

	private void addCompiledRegex(List<CompiledRegexPattern> compiled, MessageDocumentRegex patternData) {
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

	private Locale parseLocale(String localeString) {
		if (localeString == null || localeString.isEmpty())
			return Locale.getDefault();

		if ("default".equalsIgnoreCase(localeString))
			return new Locale.Builder().setLanguage("default").build();

		return LocaleUtil.parseLocale(localeString);
	}

	private CompiledMessageEntry toCompiledEntry(ParsedEntry entry) {
		List<CompiledRegexPattern> regexPatterns = entry.regexPatterns();

		if (entry.localized()) {
			Map<Locale, String> translations = new LinkedHashMap<>(entry.locales());
			if (entry.defaultText() != null) {
				translations.put(parseLocale("default"), entry.defaultText());
			}
			return new CompiledMessageEntry(MessageType.MESSAGE, translations, regexPatterns);
		}

		return new CompiledMessageEntry(MessageType.TEMPLATE, entry.text(), regexPatterns);
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

			if (!translations.isEmpty()) {
				return new LocalizedEntry(translations);
			}

			if (defaultText != null) {
				return new TemplateEntry(defaultText);
			}

			return null;
		}

		String text = entry.text();
		return text != null ? new TemplateEntry(text) : null;
	}

	@SuppressWarnings("unchecked")
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
			Map<Locale, String> locales,
			List<CompiledRegexPattern> regexPatterns
	) {
	}
}
}
