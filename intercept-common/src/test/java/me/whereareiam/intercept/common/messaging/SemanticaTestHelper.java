package me.whereareiam.intercept.common.messaging;

import me.whereareiam.intercept.common.util.MessageTags;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.messaging.CompiledMessageEntry;
import me.whereareiam.semantica.Semantica;
import me.whereareiam.semantica.SemanticaConfiguration;
import me.whereareiam.semantica.TagConfiguration;
import me.whereareiam.semantica.locale.LocaleParser;
import me.whereareiam.semantica.model.SemanticLocale;
import me.whereareiam.semantica.model.translation.entry.LocalizedEntry;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.TranslationService;
import me.whereareiam.semantica.translation.base.TranslationLocale;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class SemanticaTestHelper {
	private SemanticaTestHelper() {
	}

	public static TranslationService<Locale> createService(Settings settings) {
		Settings.Performance.Cache cache = settings.getPerformance().getCache();
		LocaleParser<Locale> localeParser = SemanticLocale::wrap;

		TagConfiguration tags = TagConfiguration.builder()
				.referencePrefix(MessageTags.MESSAGE_REF_PREFIX)
				.placeholderPrefix(MessageTags.PLACEHOLDER_PREFIX)
				.conditionalIf(MessageTags.CONDITIONAL_IF)
				.conditionalElse(MessageTags.CONDITIONAL_ELSE)
				.build();

		SemanticaConfiguration<Locale> configuration = SemanticaConfiguration.<Locale>builder()
				.defaultLocale(SemanticLocale.wrap(settings.getLocale()))
				.tagConfiguration(tags)
				.performance(SemanticaConfiguration.PerformanceSettings.builder()
						.cache(SemanticaConfiguration.PerformanceSettings.CacheSettings.builder()
								.enabled(cache.isEnabled())
								.semiStaticSize(cache.getSemiStaticSize())
								.dynamicSize(cache.getDynamicSize())
								.semiStaticExpireMinutes(cache.getSemiStaticExpireMinutes())
								.dynamicExpireMinutes(cache.getDynamicExpireMinutes())
								.build())
						.prerenderStatic(settings.getPerformance().isPrerenderStatic())
						.buildDependencyGraph(settings.getPerformance().isBuildDependencyGraph())
						.logTimings(false)
						.build())
				.localeParser(localeParser)
				.build();

		return Semantica.createService(configuration);
	}

	public static void register(TranslationService<Locale> service, String key, CompiledMessageEntry entry) {
		TranslationEntry translationEntry = toTranslationEntry(entry);
		if (translationEntry != null) {
			service.register(key, translationEntry);
		}
	}

	public static TranslationEntry toTranslationEntry(CompiledMessageEntry entry) {
		if (entry == null) return null;

		if (entry.hasTranslations()) {
			Map<TranslationLocale, String> translations = new HashMap<>();
			for (Map.Entry<Locale, String> translation : entry.getTranslations().entrySet()) {
				translations.put(SemanticLocale.wrap(translation.getKey()), translation.getValue());
			}
			return new LocalizedEntry(translations);
		}

		String text = entry.getText();
		return text != null ? new TemplateEntry(text) : null;
	}
}
