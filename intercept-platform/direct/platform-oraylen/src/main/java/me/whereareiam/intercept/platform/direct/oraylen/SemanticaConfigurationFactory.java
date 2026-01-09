package me.whereareiam.intercept.platform.direct.oraylen;

import me.whereareiam.intercept.common.messaging.InterceptSemanticaLogger;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.semantica.SemanticaConfiguration;
import me.whereareiam.semantica.TagConfiguration;
import me.whereareiam.semantica.model.SemanticLocale;

import java.util.Locale;

public final class SemanticaConfigurationFactory {
	public SemanticaConfiguration<Locale> create(Settings settings, Locale defaultLocale) {
		SemanticaConfiguration.PerformanceSettings defaults = SemanticaConfiguration.PerformanceSettings.defaults();
		SemanticaConfiguration.PerformanceSettings.CacheSettings cacheDefaults =
				SemanticaConfiguration.PerformanceSettings.CacheSettings.defaults();

		Settings.Performance performance = settings == null ? null : settings.getPerformance();
		Settings.Performance.Cache cache = performance == null ? null : performance.getCache();

		SemanticaConfiguration.PerformanceSettings.CacheSettings cacheSettings =
				SemanticaConfiguration.PerformanceSettings.CacheSettings.builder()
						.enabled(cache != null ? cache.isEnabled() : cacheDefaults.isEnabled())
						.semiStaticSize(cache != null ? cache.getSemiStaticSize() : cacheDefaults.getSemiStaticSize())
						.dynamicSize(cache != null ? cache.getDynamicSize() : cacheDefaults.getDynamicSize())
						.semiStaticExpireMinutes(cache != null
								? cache.getSemiStaticExpireMinutes()
								: cacheDefaults.getSemiStaticExpireMinutes())
						.dynamicExpireMinutes(cache != null
								? cache.getDynamicExpireMinutes()
								: cacheDefaults.getDynamicExpireMinutes())
						.build();

		boolean prerender = performance != null ? performance.isPrerenderStatic() : defaults.isPrerenderStatic();
		boolean buildGraph = performance != null ? performance.isBuildDependencyGraph() : defaults.isBuildDependencyGraph();

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
