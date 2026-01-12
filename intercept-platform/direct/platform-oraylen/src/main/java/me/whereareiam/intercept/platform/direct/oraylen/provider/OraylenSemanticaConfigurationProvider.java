package me.whereareiam.intercept.platform.direct.oraylen.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import me.whereareiam.intercept.common.InterceptSemanticaLogger;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.semantica.SemanticaConfiguration;
import me.whereareiam.semantica.TagConfiguration;
import me.whereareiam.semantica.model.SemanticLocale;

import java.util.Locale;

public final class OraylenSemanticaConfigurationProvider implements Provider<SemanticaConfiguration<Locale>> {
	private final Provider<Locale> defaultLocaleProvider;
	private final Settings settings;

	@Inject
	public OraylenSemanticaConfigurationProvider(
			@Named("defaultLocale") Provider<Locale> defaultLocaleProvider,
			Settings settings
	) {
		this.settings = settings;
		this.defaultLocaleProvider = defaultLocaleProvider;
	}

	@Override
	public SemanticaConfiguration<Locale> get() {
		Locale defaultLocale = defaultLocaleProvider.get();

		Settings.Performance performance = settings.getPerformance();
		Settings.Performance.Cache cache = performance.getCache();

		SemanticaConfiguration.PerformanceSettings.CacheSettings cacheSettings =
				SemanticaConfiguration.PerformanceSettings.CacheSettings.builder()
						.enabled(cache.isEnabled())
						.semiStaticSize(cache.getSemiStaticSize())
						.dynamicSize(cache.getDynamicSize())
						.semiStaticExpireMinutes(cache.getSemiStaticExpireMinutes())
						.dynamicExpireMinutes(cache.getDynamicExpireMinutes())
						.build();

		boolean prerender = performance.isPrerenderStatic();
		boolean buildGraph = performance.isBuildDependencyGraph();

		SemanticaConfiguration.PerformanceSettings performanceSettings =
				SemanticaConfiguration.PerformanceSettings.builder()
						.cache(cacheSettings)
						.prerenderStatic(prerender)
						.buildDependencyGraph(buildGraph)
						.logTimings(false)
						.build();

		return SemanticaConfiguration.<Locale>builder()
				.defaultLocale(SemanticLocale.wrap(defaultLocale))
				.tagConfiguration(TagConfiguration.defaults())
				.performance(performanceSettings)
				.localeParser(SemanticLocale::wrap)
				.logger(new InterceptSemanticaLogger())
				.build();
	}
}
