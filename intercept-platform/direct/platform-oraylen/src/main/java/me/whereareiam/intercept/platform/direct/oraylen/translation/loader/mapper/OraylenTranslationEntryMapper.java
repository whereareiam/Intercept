package me.whereareiam.intercept.platform.direct.oraylen.translation.loader.mapper;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.translation.mapper.TranslationEntryMapper;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.model.messaging.file.MessageValue;
import me.whereareiam.intercept.util.LocaleUtil;
import me.whereareiam.semantica.model.SemanticLocale;
import me.whereareiam.semantica.model.translation.entry.LocalizedEntry;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.base.TranslationLocale;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class OraylenTranslationEntryMapper implements TranslationEntryMapper<MessageFileData> {
	private final Provider<Locale> defaultLocaleProvider;

	@Inject
	public OraylenTranslationEntryMapper(@Named("defaultLocale") Provider<Locale> defaultLocaleProvider) {
		this.defaultLocaleProvider = defaultLocaleProvider;
	}

	@Override
	public Map<String, TranslationEntry> map(String namespace, MessageFileData document) {
		Map<String, TranslationEntry> entries = new HashMap<>();
		if (document == null || document.getEntries().isEmpty()) return entries;
		parseEntries("", document.getEntries(), entries, namespace);
		return entries;
	}

	private void parseEntries(
			String prefix,
			Map<String, MessageFileData.Node> source,
			Map<String, TranslationEntry> target,
			String namespace
	) {
		for (Map.Entry<String, MessageFileData.Node> entry : source.entrySet()) {
			String key = entry.getKey();
			MessageFileData.Node node = entry.getValue();
			if (key == null || node == null) continue;

			String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

			if (node instanceof MessageFileData.Entry fileEntry) {
				TranslationEntry mapped = toTranslationEntry(fileEntry);
				if (mapped != null) {
					target.put(qualifyKey(namespace, fullKey), mapped);
				}

				continue;
			}

			if (node instanceof MessageFileData.Section section)
				parseEntries(fullKey, section.getEntries(), target, namespace);
		}
	}

	private TranslationEntry toTranslationEntry(MessageFileData.Entry entry) {
		Map<String, MessageValue> locales = entry.getLocales();
		if (locales != null && !locales.isEmpty()) {
			Map<TranslationLocale, String> translated = new HashMap<>();
			for (Map.Entry<String, MessageValue> localeEntry : locales.entrySet()) {
				String localeKey = localeEntry.getKey();
				String text = toText(localeEntry.getValue());
				if (localeKey == null || text == null) continue;

				Locale locale = resolveLocale(localeKey);
				if (locale != null) {
					translated.put(SemanticLocale.wrap(locale), text);
				}
			}

			if (!translated.isEmpty())
				return new LocalizedEntry(translated);
		}

		String text = toText(entry.getText());
		return text != null ? new TemplateEntry(text) : null;
	}

	private String toText(MessageValue value) {
		return switch (value) {
			case null -> null;
			case MessageValue.Text(String value1) -> value1;
			case MessageValue.Lines(java.util.List<String> values) -> String.join("\n", values);
		};
	}

	private Locale resolveLocale(String localeKey) {
		if (localeKey == null || localeKey.isBlank()) return null;
		if ("default".equalsIgnoreCase(localeKey))
			return defaultLocaleProvider.get();

		return LocaleUtil.parseLocale(localeKey);
	}

	private String qualifyKey(String namespace, String key) {
		if (key == null || key.isBlank()) return key;
		if (key.contains(Constants.Namespace.NAMESPACE_SEPARATOR)) return key;
		return namespace + Constants.Namespace.NAMESPACE_SEPARATOR + key;
	}
}
