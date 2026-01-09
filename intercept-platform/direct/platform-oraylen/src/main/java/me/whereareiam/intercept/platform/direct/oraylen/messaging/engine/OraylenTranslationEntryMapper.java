package me.whereareiam.intercept.platform.direct.oraylen.messaging.engine;

import me.whereareiam.intercept.common.messaging.TranslationEntryMapper;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.platform.direct.common.translation.model.TranslationDocument;
import me.whereareiam.semantica.model.SemanticLocale;
import me.whereareiam.semantica.model.translation.entry.LocalizedEntry;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.base.TranslationLocale;

import java.util.HashMap;
import java.util.Map;

public final class OraylenTranslationEntryMapper implements TranslationEntryMapper<TranslationDocument> {
	@Override
	public Map<String, TranslationEntry> map(String namespace, TranslationDocument document) {
		Map<String, TranslationEntry> entries = new HashMap<>();

		if (document == null) return entries;

		document.getEntries().forEach((key, entry) -> {
			String qualified = qualifyKey(namespace, key);
			if (qualified == null || qualified.isBlank()) return;

			switch (entry.getType()) {
				case TEMPLATE -> {
					if (entry.getTemplateValue() != null) {
						entries.put(qualified, new TemplateEntry(entry.getTemplateValue().asString()));
					}
				}
				case LOCALIZED -> {
					if (!entry.getLocalizedValues().isEmpty()) {
						Map<TranslationLocale, String> translated = new HashMap<>();
						entry.getLocalizedValues().forEach((locale, text) -> {
							if (locale == null || text == null) return;
							translated.put(SemanticLocale.wrap(locale), text.asString());
						});
						if (!translated.isEmpty()) {
							entries.put(qualified, new LocalizedEntry(translated));
						}
					}
				}
			}
		});

		return entries;
	}

	private String qualifyKey(String namespace, String key) {
		if (key == null || key.isBlank()) return key;
		if (key.contains(Constants.Namespace.NAMESPACE_SEPARATOR)) return key;

		return namespace + Constants.Namespace.NAMESPACE_SEPARATOR + key;
	}
}
