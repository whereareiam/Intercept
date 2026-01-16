package me.whereareiam.intercept.platform.interception;

import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.semantica.Semantica;
import me.whereareiam.semantica.SemanticaConfiguration;
import me.whereareiam.semantica.TagConfiguration;
import me.whereareiam.semantica.locale.LocaleParser;
import me.whereareiam.semantica.model.SemanticLocale;
import me.whereareiam.semantica.model.translation.entry.LocalizedEntry;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.TranslationRegistry;
import me.whereareiam.semantica.translation.TranslationService;
import me.whereareiam.semantica.translation.base.TranslationLocale;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class SemanticaTestHelper {
	public static TranslationService<Locale> createService(Settings settings, TranslationRegistry registry) {
		Settings.Performance.Cache cache = settings.getPerformance().getCache();
		LocaleParser<Locale> localeParser = SemanticLocale::wrap;

		SemanticaConfiguration<Locale> configuration = SemanticaConfiguration.<Locale>builder()
				.defaultLocale(SemanticLocale.wrap(Locale.ENGLISH))
				.tagConfiguration(TagConfiguration.defaults())
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

		if (registry != null) {
			return Semantica.createService(configuration, registry);
		}

		return Semantica.createService(configuration);
	}

	public static void register(TranslationService<Locale> service, String key, TranslationEntry entry) {
		if (entry != null) {
			service.register(key, entry);
		}
	}

	public static TranslationEntry template(String text) {
		return text != null ? new TemplateEntry(text) : null;
	}

	public static TranslationEntry localized(Map<Locale, String> translations) {
		if (translations == null || translations.isEmpty()) {
			return null;
		}

		Map<TranslationLocale, String> mapped = new HashMap<>();
		for (Map.Entry<Locale, String> entry : translations.entrySet()) {
			mapped.put(SemanticLocale.wrap(entry.getKey()), entry.getValue());
		}
		return new LocalizedEntry(mapped);
	}
}
