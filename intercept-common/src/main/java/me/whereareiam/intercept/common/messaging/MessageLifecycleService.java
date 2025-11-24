package me.whereareiam.intercept.common.messaging;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.messaging.cache.CacheKey;
import me.whereareiam.intercept.common.messaging.cache.CacheLevel;
import me.whereareiam.intercept.common.messaging.cache.CacheStrategy;
import me.whereareiam.intercept.common.messaging.cache.MessageCache;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.messaging.MessageDataService;
import me.whereareiam.intercept.messaging.MessageService;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.messaging.CompiledMessageEntry;
import me.whereareiam.intercept.registry.Registry;

import java.util.Locale;
import java.util.Map;

/**
 * Service that orchestrates message file loading.
 * Handles initialization and loading of all message files from the messages directory.
 */
@Singleton
public class MessageLifecycleService implements Reloadable {
	private final MessageDataService messageDataService;
	private final DefaultMessageRegistry registry;
	private final Provider<Settings> settingsProvider;
	private final MessageService messageService;
	private final DependencyGraph dependencyGraph;
	private final CacheStrategy cacheStrategy;

	@Inject
	public MessageLifecycleService(
			MessageDataService messageDataService,
			DefaultMessageRegistry registry,
			Provider<Settings> settingsProvider,
			MessageService messageService,
			Registry<Reloadable> reloadableRegistry
	) {
		this.messageDataService = messageDataService;
		this.registry = registry;
		this.settingsProvider = settingsProvider;
		this.messageService = messageService;

		this.dependencyGraph = new DependencyGraph();
		this.cacheStrategy = new CacheStrategy();

		reloadableRegistry.register(this);
	}

	/**
	 * Initialize the messages system by loading all message files.
	 */
	public void initialize() {
		// Use MessageDataService to load all files
		messageDataService.initialize();

		// Post-loading optimizations
		Settings settings = settingsProvider.get();

		boolean shouldBuildGraph = settings.getPerformance().isBuildDependencyGraph();
		boolean shouldPrerender = settings.getPerformance().isPrerenderStatic() && settings.getPerformance().getCache().isEnabled();

		if (shouldBuildGraph || shouldPrerender) {
			long startTime = System.currentTimeMillis();

			if (shouldBuildGraph) buildDependencyGraph();
			if (shouldPrerender) prerenderStaticMessages();

			long duration = System.currentTimeMillis() - startTime;
			Logger.info("Finished compiling in %d ms", duration);
		}
	}

	private void buildDependencyGraph() {
		Logger.debug("Building dependency graph...");

		Map<String, CompiledMessageEntry> entries = registry.getAllEntries();
		dependencyGraph.build(entries);

		// Log warnings for circular dependencies
		for (String key : entries.keySet())
			if (dependencyGraph.hasCircularDependency(key))
				Logger.warn("Circular dependency detected for message key: %s", key);

		Logger.debug("Dependency graph built for %d entries", entries.size());
	}

	private void prerenderStaticMessages() {
		Settings settings = settingsProvider.get();
		Settings.Performance.Cache cacheConfig = settings.getPerformance().getCache();

		MessageCache cache = new MessageCache(
				cacheConfig.getSemiStaticSize(),
				cacheConfig.getDynamicSize(),
				cacheConfig.getSemiStaticExpireMinutes(),
				cacheConfig.getDynamicExpireMinutes()
		);

		int preRendered = 0;
		for (Map.Entry<String, CompiledMessageEntry> entry : registry.getAllEntries().entrySet()) {
			String key = entry.getKey();
			CompiledMessageEntry compiledMessageEntry = entry.getValue();

			// Get text for classification
			String text = compiledMessageEntry.hasTranslations()
					? compiledMessageEntry.getText(settings.getLocale())
					: compiledMessageEntry.getText();

			if (text == null) continue;

			// Only pre-render fully static messages
			CacheLevel level = cacheStrategy.classify(text);
			if (level == CacheLevel.STATIC) {
				// Pre-render for all available locales
				if (compiledMessageEntry.hasTranslations()) {
					for (Locale locale : compiledMessageEntry.getLocales()) {
						String resolved = messageService.resolve(key, locale, Map.of());
						if (resolved != null) {
							cache.put(new CacheKey(key, locale, Map.of()), resolved, CacheLevel.STATIC);
							preRendered++;
						}
					}
					continue;
				}

				String resolved = messageService.resolve(key, settings.getLocale(), Map.of());
				if (resolved != null) {
					cache.put(new CacheKey(key, settings.getLocale(), Map.of()), resolved, CacheLevel.STATIC);
					preRendered++;
				}
			}
		}

		Logger.debug("Pre-rendered %d static messages", preRendered);
	}

	@Override
	public void reload() {
		messageDataService.reload();
		initialize();
	}
}